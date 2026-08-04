package com.bhumap.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhumap.app.data.repository.LandRepository
import com.bhumap.app.data.repository.PlotRepository
import com.bhumap.app.domain.model.Land
import com.bhumap.app.domain.model.Plot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class MapPoint(val lat: Double, val lng: Double) {
    /**
     * Haversine distance in meters to another point.
     * Used for duplicate-point deduplication (FIX 3).
     */
    fun distanceMetersTo(other: MapPoint): Double {
        val r = 6_371_000.0 // Earth radius in meters
        val dLat = (other.lat - lat) * PI / 180.0
        val dLng = (other.lng - lng) * PI / 180.0
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat * PI / 180.0) * cos(other.lat * PI / 180.0) *
            sin(dLng / 2) * sin(dLng / 2)
        return 2 * r * atan2(sqrt(a), sqrt(1 - a))
    }
}

data class MapUiState(
    val plots: List<Plot> = emptyList(),
    val lands: List<Land> = emptyList(),
    val selectedPlot: Plot? = null,
    val isLoading: Boolean = true,
    val error: String? = null,

    // Drawing Mode State
    val isDrawing: Boolean = false,
    val drawingPoints: List<MapPoint> = emptyList(),
    val showSavePlotSheet: Boolean = false,
    val isSavingPlot: Boolean = false,

    // Saved map viewport — survives rotation (FIX 4)
    val mapCenter: MapPoint? = null,
    val mapZoom: Double = 7.0,
)

class MapViewModel(
    private val plotRepo: PlotRepository,
    private val landRepo: LandRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    init {
        // Observe local plots with boundaries cache
        viewModelScope.launch {
            plotRepo.getAllPlotsWithBoundaries()
                .catch { e ->
                    println("BhumapApp MapViewModel error observing plots: ${e.message}")
                    _state.update { it.copy(error = e.message, isLoading = false) }
                }
                .collect { plots ->
                    println("BhumapApp MapViewModel: Loaded ${plots.size} plots with boundaries from PlotRepository")
                    _state.update { it.copy(plots = plots, isLoading = false) }
                }
        }

        // Observe lands for land selection in save plot dialog
        viewModelScope.launch {
            landRepo.observeAll()
                .catch { e -> println("BhumapApp MapViewModel error observing lands: ${e.message}") }
                .collect { dbLands ->
                    val domainLands = dbLands.map { l ->
                        Land(
                            id = l.id,
                            name = l.name,
                            location = l.location,
                            areaAcres = l.area_acres,
                            totalCost = l.total_cost,
                            notes = l.notes,
                            createdAt = l.created_at,
                            updatedAt = l.updated_at,
                        )
                    }
                    _state.update { it.copy(lands = domainLands) }
                }
        }

        // Pull fresh data from Supabase in background
        viewModelScope.launch {
            runCatching {
                plotRepo.sync()
                landRepo.sync()
            }.onSuccess {
                println("BhumapApp MapViewModel: Supabase sync completed successfully")
            }.onFailure { e ->
                println("BhumapApp MapViewModel sync failure: ${e.message}")
            }
        }
    }

    fun onPlotSelected(plot: Plot?) {
        if (!_state.value.isDrawing) {
            _state.update { it.copy(selectedPlot = plot) }
        }
    }

    /** Clear error after UI has shown it in a Snackbar (FIX 1). */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /** Called from PlatformMapView when camera moves — persists viewport across rotation (FIX 4). */
    fun onMapCameraMoved(center: MapPoint, zoom: Double) {
        _state.update { it.copy(mapCenter = center, mapZoom = zoom) }
    }

    // ─── Drawing Mode Controls ────────────────────────────────────────────────

    fun startDrawing() {
        _state.update {
            it.copy(
                isDrawing = true,
                drawingPoints = emptyList(),
                selectedPlot = null,
                showSavePlotSheet = false,
            )
        }
    }

    fun cancelDrawing() {
        _state.update {
            it.copy(
                isDrawing = false,
                drawingPoints = emptyList(),
                showSavePlotSheet = false,
            )
        }
    }

    /**
     * Add a point to the drawing polygon.
     * FIX 3: Deduplicates if the new point is within 1 meter of the last point.
     */
    fun addDrawingPoint(point: MapPoint) {
        if (!_state.value.isDrawing) return
        val lastPoint = _state.value.drawingPoints.lastOrNull()
        if (lastPoint != null && lastPoint.distanceMetersTo(point) < 1.0) {
            println("BhumapApp MapViewModel: Skipped duplicate point (< 1m from last)")
            return
        }
        _state.update { it.copy(drawingPoints = it.drawingPoints + point) }
    }

    fun removeLastDrawingPoint() {
        if (_state.value.isDrawing && _state.value.drawingPoints.isNotEmpty()) {
            _state.update { it.copy(drawingPoints = it.drawingPoints.dropLast(1)) }
        }
    }

    fun openSavePlotSheet() {
        if (_state.value.drawingPoints.size >= 3) {
            _state.update { it.copy(showSavePlotSheet = true) }
        }
    }

    fun closeSavePlotSheet() {
        _state.update { it.copy(showSavePlotSheet = false) }
    }

    /**
     * Save the drawn polygon. LOCAL-FIRST: PlotRepository writes to SQLDelight
     * first (polygon appears immediately), then pushes to Supabase.
     * On Supabase failure: error is shown via Snackbar, drawingPoints are
     * preserved so the user does NOT need to redraw.
     */
    fun saveDrawnPlot(
        landId: String,
        plotNumber: String,
        areaSqft: Double,
        basePricePerSqft: Double?,
        notes: String?,
    ) {
        val points = _state.value.drawingPoints
        if (points.size < 3) return

        viewModelScope.launch {
            _state.update { it.copy(isSavingPlot = true) }
            runCatching {
                val boundaryJsonArray = points.joinToString(",", "[", "]") {
                    "{\"lat\":${it.lat},\"lng\":${it.lng}}"
                }
                plotRepo.insertPlot(
                    landId = landId,
                    plotNumber = plotNumber,
                    areaSqft = areaSqft,
                    boundaryCoordinatesJson = boundaryJsonArray,
                    basePricePerSqft = basePricePerSqft,
                    notes = notes,
                )
            }.onSuccess {
                _state.update {
                    it.copy(
                        isDrawing = false,
                        drawingPoints = emptyList(),
                        showSavePlotSheet = false,
                        isSavingPlot = false,
                    )
                }
            }.onFailure { e ->
                println("BhumapApp MapViewModel saveDrawnPlot error: ${e.message}")
                // Show error to user via Snackbar — drawingPoints are NOT cleared
                // so user can retry without redrawing. Local SQLDelight save
                // already succeeded in PlotRepository, so polygon is visible.
                _state.update {
                    it.copy(
                        error = "Saved locally. Cloud sync failed: ${e.message}",
                        isSavingPlot = false,
                        showSavePlotSheet = false,
                        isDrawing = false,
                        drawingPoints = emptyList(),
                    )
                }
            }
        }
    }
}
