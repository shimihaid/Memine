package com.example.lasttime.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lasttime.data.repository.ActivityRepository
import com.example.lasttime.data.repository.CalendarRepository
import com.example.lasttime.domain.model.Activity
import com.example.lasttime.domain.model.Holiday
import com.example.lasttime.domain.model.MarkOutcome
import com.example.lasttime.domain.model.toUserMessage
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val activities: List<Activity>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface HolidayUiState {
    data object Loading : HolidayUiState
    data class Loaded(val holiday: Holiday?) : HolidayUiState
    data class Error(val message: String) : HolidayUiState
}

sealed interface HomeEvent {
    data class MarkToday(val activityId: Long) : HomeEvent
    data class Delete(val activityId: Long) : HomeEvent
    data class AddActivity(
        val name: String,
        val date: LocalDate,
        val category: String,
        val notes: String
    ) : HomeEvent
    data object RetryHoliday : HomeEvent
}

class HomeViewModel(
    private val activityRepository: ActivityRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = activityRepository.observeActivities()
        .map<List<Activity>, HomeUiState> { HomeUiState.Success(it) }
        .catch { emit(HomeUiState.Error("Não foi possível carregar as atividades.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    private val _holidayState = MutableStateFlow<HolidayUiState>(HolidayUiState.Loading)
    val holidayState: StateFlow<HolidayUiState> = _holidayState.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    init {
        viewModelScope.launch {
            activityRepository.seedInitialDataIfEmpty()
        }
        loadHoliday()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.MarkToday -> markToday(event.activityId)
            is HomeEvent.Delete -> delete(event.activityId)
            is HomeEvent.AddActivity -> addActivity(event.name, event.date, event.category, event.notes)
            HomeEvent.RetryHoliday -> loadHoliday()
        }
    }

    private fun addActivity(name: String, date: LocalDate, category: String, notes: String) {
        viewModelScope.launch {
            activityRepository.addActivity(name, date, category, notes).fold(
                onSuccess = { _messages.send("Atividade salva com sucesso.") },
                onFailure = { _messages.send(it.toUserMessage("Não foi possível salvar a atividade.")) }
            )
        }
    }

    private fun markToday(activityId: Long) {
        viewModelScope.launch {
            activityRepository.markPerformedToday(activityId).fold(
                onSuccess = { outcome ->
                    _messages.send(
                        when (outcome) {
                            MarkOutcome.MARKED -> "Atividade marcada como realizada hoje."
                            MarkOutcome.ALREADY_MARKED_TODAY -> "Você já marcou essa atividade hoje."
                            MarkOutcome.NOT_FOUND -> "Atividade não encontrada."
                        }
                    )
                },
                onFailure = {
                    _messages.send(it.toUserMessage("Não foi possível atualizar a atividade."))
                }
            )
        }
    }

    private fun delete(activityId: Long) {
        viewModelScope.launch {
            activityRepository.deleteActivity(activityId).fold(
                onSuccess = { _messages.send("Atividade excluída.") },
                onFailure = {
                    _messages.send(it.toUserMessage("Não foi possível excluir a atividade."))
                }
            )
        }
    }

    private fun loadHoliday() {
        viewModelScope.launch {
            _holidayState.value = HolidayUiState.Loading
            calendarRepository.getNextHoliday(LocalDate.now()).fold(
                onSuccess = { _holidayState.value = HolidayUiState.Loaded(it) },
                onFailure = {
                    _holidayState.value = HolidayUiState.Error(
                        it.toUserMessage("Não foi possível carregar o próximo feriado.")
                    )
                }
            )
        }
    }
}
