package com.bhumap.app.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhumap.app.data.repository.PlotRepository
import com.bhumap.app.domain.model.Plot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapUiState(
    val plots: List<Plot> = emptyList(),
    val selectedPlot: Plot? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

class MapViewModel(private val plotRepo: PlotRepository) : ViewModel() {

    private val _state = MutableStateFlow(MapUiState())
    val state: StateFlow<MapUiState> = _state.asStateFlow()

    init {
        // Observe local cache — emits immediately, then on every DB write
        viewModelScope.launch {
            plotRepo.getAllPlotsWithBoundaries()
                .catch { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
                .collect { plots -> _state.update { it.copy(plots = plots, isLoading = false) } }
        }
