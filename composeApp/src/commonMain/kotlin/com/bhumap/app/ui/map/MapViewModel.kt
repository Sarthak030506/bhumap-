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

enum class DrawMode {
    NONE,
    SELECTING_TYPE,
    SELECTING_LAND,
    DRAWING_LAND,
    DRAWING_PLOT,
}

data class MapPoint(val lat: Double, val lng: Double) {
    /**
     * Haversine distance in meters to another point.
     * Used for duplicate-point deduplication.
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
    val drawMode: DrawMode = DrawMode.NONE,
    val selectedParentLand: Land? = null,
    val isDrawing: Boolean = false,
    val drawingPoints: List<MapPoint> = emptyList(),
    val showSavePlotSheet: Boolean = false,
    val isSavingPlot: Boolean = false,

    // Saved map viewport — survives rotation
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

    /** Clear error after UI has shown it in a Snackbar. */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    /** Called from PlatformMapView when camera moves — persists viewport across rotation. */
    fun onMapCameraMoved(center: MapPoint, zoom: Double) {
        _state.update { it.copy(mapCenter = center, mapZoom = zoom) }
    }

    // ─── Drawing Mode Controls ────────────────────────────────────────────────

    /** Step 1: User taps "Draw" FAB → show type selector bottom sheet */
    fun onDrawFabTapped() {
        _state.update {
            it.copy(
                drawMode = DrawMode.SELECTING_TYPE,
                selectedPlot = null,
            )
        }
    }

    /** Step 2A: User selects "Land Boundary" → enter land drawing mode */
    fun onDrawTypeLand() {
        _state.update {
            it.copy(
                drawMode = DrawMode.DRAWING_LAND,
                isDrawing = true,
                drawingPoints = emptyList(),
                selectedParentLand = null,
            )
        }
    }

    /** Step 2B: User selects "Plot" → show parent land selector sheet */
    fun onDrawTypePlot() {
        _state.update {
            it.copy(
                drawMode = DrawMode.SELECTING_LAND,
            )
        }
    }

    /** Step 3: User selects parent land → compute centroid, animate map to zoom 17, enter plot drawing mode */
    fun onParentLandSelected(land: Land) {
        val centroid = calculateLandCentroid(land)
        _state.update {
            it.copy(
                selectedParentLand = land,
                drawMode = DrawMode.DRAWING_PLOT,
                isDrawing = true,
                drawingPoints = emptyList(),
                mapCenter = centroid ?: it.mapCenter,
                mapZoom = if (centroid != null) 17.0 else it.mapZoom,
            )
        }
    }

    fun cancelDrawing() {
        _state.update {
            it.copy(
                drawMode = DrawMode.NONE,
                isDrawing = false,
                drawingPoints = emptyList(),
                showSavePlotSheet = false,
                selectedParentLand = null,
            )
        }
    }

    /**
     * Add a point to the drawing polygon.
     * Deduplicates if the new point is within 1 meter of the last point.
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

    /** Called when user taps "Complete" button after placing >= 3 points */
    fun onDrawComplete(onNavigateToAddLand: (String) -> Unit) {
        val points = _state.value.drawingPoints
        if (points.size < 3) return

        val boundaryJsonArray = points.joinToString(",", "[", "]") {
            "{\"lat\":${it.lat},\"lng\":${it.lng}}"
        }

        when (_state.value.drawMode) {
            DrawMode.DRAWING_LAND -> {
                _state.update {
                    it.copy(
                        drawMode = DrawMode.NONE,
                        isDrawing = false,
                        drawingPoints = emptyList(),
                    )
                }
                onNavigateToAddLand(boundaryJsonArray)
            }
            DrawMode.DRAWING_PLOT -> {
                openSavePlotSheet()
            }
            else -> {}
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
     * Save the drawn plot. LOCAL-FIRST: PlotRepository writes to SQLDelight
     * first (polygon appears immediately), then pushes to Supabase.
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
        val effectiveLandId = _state.value.selectedParentLand?.id ?: landId

        viewModelScope.launch {
            _state.update { it.copy(isSavingPlot = true) }
            runCatching {
                val boundaryJsonArray = points.joinToString(",", "[", "]") {
                    "{\"lat\":${it.lat},\"lng\":${it.lng}}"
                }
                plotRepo.insertPlot(
                    landId = effectiveLandId,
                    plotNumber = plotNumber,
                    areaSqft = areaSqft,
                    boundaryCoordinatesJson = boundaryJsonArray,
                    basePricePerSqft = basePricePerSqft,
                    notes = notes,
                )
            }.onSuccess {
                _state.update {
                    it.copy(
                        drawMode = DrawMode.NONE,
                        isDrawing = false,
                        drawingPoints = emptyList(),
                        showSavePlotSheet = false,
                        isSavingPlot = false,
                        selectedParentLand = null,
                    )
                }
            }.onFailure { e ->
                println("BhumapApp MapViewModel saveDrawnPlot error: ${e.message}")
                _state.update {
                    it.copy(
                        error = "Saved locally. Cloud sync failed: ${e.message}",
                        isSavingPlot = false,
                        showSavePlotSheet = false,
                        drawMode = DrawMode.NONE,
                        isDrawing = false,
                        drawingPoints = emptyList(),
                        selectedParentLand = null,
                    )
                }
            }
        }
    }
}

/** Compute centroid average of all lat/lng boundary points */
private fun calculateLandCentroid(land: Land): MapPoint? {
    val json = land.boundaryJson ?: return null
    if (json.isBlank()) return null
    return try {
        val latRegex = "\"lat\"\\s*:\\s*(-?\\d+\\.\\d+)".toRegex()
        val lngRegex = "\"lng\"\\s*:\\s*(-?\\d+\\.\\d+)".toRegex()
        val lats = latRegex.findAll(json).map { it.groupValues[1].toDouble() }.toList()
        val lngs = lngRegex.findAll(json).map { it.groupValues[1].toDouble() }.toList()
        val count = minOf(lats.size, lngs.size)
        if (count == 0) null
        else {
            val avgLat = lats.take(count).sum() / count
            val avgLng = lngs.take(count).sum() / count
            MapPoint(avgLat, avgLng)
        }
    } catch (_: Exception) {
        null
    }
}
