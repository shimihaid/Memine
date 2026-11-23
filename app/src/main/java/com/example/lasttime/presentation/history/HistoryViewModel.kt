package com.example.lasttime.presentation.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lasttime.data.repository.ActivityRepository
import com.example.lasttime.domain.model.Activity
import com.example.lasttime.domain.model.KanbanTask
import com.example.lasttime.presentation.navigation.Routes
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data class Success(
        val activities: List<Activity>,
        val selected: Activity?,
        val dates: List<LocalDate>,
        val kanbanTasks: List<KanbanTask> = emptyList()
    ) : HistoryUiState
    data class Error(val message: String) : HistoryUiState
}

class HistoryViewModel(
    private val repository: ActivityRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Vem da rota history/{activityId}; null quando abre pela aba "Histórico".
    private val selectedId = MutableStateFlow(savedStateHandle.get<Long>(Routes.ARG_ACTIVITY_ID))

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> =
        combine(repository.observeActivities(), selectedId) { activities, id -> activities to id }
            .flatMapLatest { (activities, id) -> historyFor(activities, id) }
            .catch { emit(HistoryUiState.Error("Não foi possível carregar o histórico.")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState.Loading)

    fun onSelectActivity(id: Long) {
        selectedId.value = id
    }

    /** Adiciona uma nova sub-tarefa no Kanban da atividade */
    fun addKanbanTask(activityId: Long, title: String) {
        viewModelScope.launch {
            try {
                repository.addKanbanTask(activityId, title)
            } catch (e: Exception) {
                // Erro ignorado ou tratado
            }
        }
    }

    /** Atualiza o status de uma tarefa do Kanban (Pendente, Em andamento, Concluído) */
    fun updateKanbanTaskStatus(taskId: Long, activityId: Long, newStatus: String) {
        viewModelScope.launch {
            try {
                repository.updateKanbanTaskStatus(taskId, activityId, newStatus)
            } catch (e: Exception) {
                // Erro ignorado ou tratado
            }
        }
    }

    /** Exclui uma tarefa do Kanban */
    fun deleteKanbanTask(taskId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteKanbanTask(taskId)
            } catch (e: Exception) {
                // Erro ignorado ou tratado
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun historyFor(activities: List<Activity>, id: Long?): Flow<HistoryUiState> {
        if (activities.isEmpty()) {
            return flowOf(HistoryUiState.Success(emptyList(), null, emptyList(), emptyList()))
        }
        val selected = if (id == null) activities.first() else activities.firstOrNull { it.id == id }
        if (selected == null) {
            return flowOf(HistoryUiState.Error("Atividade não encontrada."))
        }
        return combine(
            repository.observeHistory(selected.id),
            repository.observeKanbanTasks(selected.id)
        ) { dates, kanbanTasks ->
            HistoryUiState.Success(activities, selected, dates, kanbanTasks)
        }
    }
}
