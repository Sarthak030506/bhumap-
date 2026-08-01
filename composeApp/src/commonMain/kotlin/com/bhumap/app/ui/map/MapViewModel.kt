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
