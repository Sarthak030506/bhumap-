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

    init {
        viewModelScope.launch {
            repo.observeAll()
                .catch { e -> _state.update { it.copy(error = e.message, isLoading = false) } }
                .collect { list -> _state.update { it.copy(lands = list, isLoading = false) } }
        }
        viewModelScope.launch { runCatching { repo.sync() } }
    }

    // Form fields
    fun onNameChange(v: String)     = _state.update { it.copy(formName = v) }
    fun onLocationChange(v: String) = _state.update { it.copy(formLocation = v) }
    fun onAreaChange(v: String)     = _state.update { it.copy(formArea = v) }
    fun onCostChange(v: String)     = _state.update { it.copy(formCost = v) }
    fun onNotesChange(v: String)    = _state.update { it.copy(formNotes = v) }

    val isFormValid: Boolean
        get() = _state.value.run {
            formName.isNotBlank() && formLocation.isNotBlank()
                && formArea.toDoubleOrNull() != null
                && formCost.toDoubleOrNull() != null
        }

    fun saveLand(onSuccess: () -> Unit) {
        val s = _state.value
        _state.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            val now = Clock.System.now().toString()
            runCatching {
                repo.insert(
                    DomainLand(
                        id         = Uuid.random().toString(),
                        name       = s.formName.trim(),
                        location   = s.formLocation.trim(),
                        areaAcres  = s.formArea.toDouble(),
                        totalCost  = s.formCost.toDouble(),
                        notes      = s.formNotes.ifBlank { null },
                        createdAt  = now,
                        updatedAt  = now,
                    )
                )
            }.onSuccess {
                _state.update { it.copy(isSaving = false) }
                onSuccess()
            }.onFailure { e ->
                _state.update { it.copy(isSaving = false, saveError = e.message) }
            }
        }
    }
}
