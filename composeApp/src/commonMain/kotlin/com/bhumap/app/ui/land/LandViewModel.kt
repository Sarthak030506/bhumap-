package com.bhumap.app.ui.land

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhumap.app.data.local.db.Land
import com.bhumap.app.data.repository.LandRepository
import com.bhumap.app.domain.model.Land as DomainLand
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class LandUiState(
    val lands: List<Land>  = emptyList(),
    val isLoading: Boolean = true,
    val error: String?     = null,
    // Add-land form state
    val formName: String     = "",
    val formLocation: String = "",
    val formArea: String     = "",
    val formCost: String     = "",
    val formNotes: String    = "",
    val isSaving: Boolean    = false,
    val saveError: String?   = null,
)

@OptIn(ExperimentalUuidApi::class)
class LandViewModel(private val repo: LandRepository) : ViewModel() {

    private val _state = MutableStateFlow(LandUiState())
    val state: StateFlow<LandUiState> = _state.asStateFlow()
