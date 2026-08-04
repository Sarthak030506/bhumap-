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

data class MapPoint(val lat: Double, val lng: Double)

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

    fun addDrawingPoint(point: MapPoint) {
        if (_state.value.isDrawing) {
            _state.update { it.copy(drawingPoints = it.drawingPoints + point) }
        }
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
                _state.update { it.copy(error = e.message, isSavingPlot = false) }
            }
        }
    }
}
