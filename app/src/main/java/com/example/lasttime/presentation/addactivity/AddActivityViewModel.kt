package com.example.lasttime.presentation.addactivity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lasttime.data.repository.ActivityRepository
import com.example.lasttime.domain.model.ActivityRules
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Estado da tela de criação de atividade.
 */
data class AddActivityUiState(
    val name: String = "",
    val date: LocalDate = LocalDate.now(),
    val nameError: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false
)

/**
 * ViewModel simples para gerenciamento da tela de criação.
 */
class AddActivityViewModel(private val repository: ActivityRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AddActivityUiState())
    val uiState: StateFlow<AddActivityUiState> = _uiState.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    fun onNameChanged(newName: String) {
        _uiState.update { it.copy(name = newName, nameError = null) }
    }

    fun onDateChanged(newDate: LocalDate) {
        _uiState.update { it.copy(date = newDate) }
    }

    fun save() {
        val current = _uiState.value
        if (current.isSaving) return

        val nameError = ActivityRules.validateName(current.name)
        if (nameError != null) {
            _uiState.update { it.copy(nameError = nameError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                repository.addActivity(current.name, current.date)
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false) }
                _messages.send(e.message ?: "Não foi possível salvar a atividade.")
            }
        }
    }
}
