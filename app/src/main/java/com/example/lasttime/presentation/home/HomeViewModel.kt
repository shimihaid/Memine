package com.example.lasttime.presentation.home

// Importações do ecossistema Android Jetpack e ViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

// Repositórios e modelos de domínio
import com.example.lasttime.data.repository.ActivityRepository
import com.example.lasttime.data.repository.CalendarRepository
import com.example.lasttime.domain.model.Activity
import com.example.lasttime.domain.model.Holiday
import com.example.lasttime.domain.model.MarkOutcome
import com.example.lasttime.domain.model.toUserMessage

// Corrotinas, Canais de mensagens e Operadores Reativos (Flow/StateFlow)
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Representa os estados possíveis da interface da tela inicial (Home).
 */
sealed interface HomeUiState {
    /** Estado de carregamento dos dados. */
    data object Loading : HomeUiState

    /**
     * Estado de sucesso com os dados carregados do banco local.
     *
     * @property activities Lista de atividades salvas.
     * @property categories Lista de categorias cadastradas.
     */
    data class Success(
        val activities: List<Activity>,
        val categories: List<String>
    ) : HomeUiState

    /**
     * Estado de erro exibindo uma mensagem amigável para o usuário.
     *
     * @property message Texto da mensagem de erro.
     */
    data class Error(val message: String) : HomeUiState
}

/**
 * Representa o estado do componente do próximo feriado (API externa de calendário).
 */
sealed interface HolidayUiState {
    data object Loading : HolidayUiState
    data class Loaded(val holiday: Holiday?) : HolidayUiState
    data class Error(val message: String) : HolidayUiState
}

/**
 * Eventos disparados pela interface para acionar ações de negócio na ViewModel.
 */
sealed interface HomeEvent {
    /** Disparado para marcar a atividade como realizada na data de hoje. */
    data class MarkToday(val activityId: Long) : HomeEvent

    /** Disparado para remover uma atividade pelo ID. */
    data class Delete(val activityId: Long) : HomeEvent

    /** Disparado para salvar uma nova atividade com dados informados no modal. */
    data class AddActivity(
        val name: String,
        val date: LocalDate,
        val category: String,
        val notes: String
    ) : HomeEvent

    /** Disparado para cadastrar uma nova categoria personalizada. */
    data class AddCategory(val name: String) : HomeEvent

    /** Disparado para reprocessar a consulta do próximo feriado na API externa. */
    data object RetryHoliday : HomeEvent
}

/**
 * ViewModel responsável por gerenciar os dados da tela principal (Home).
 *
 * Mapeia os fluxos do Room em um [StateFlow] reativo único, lida com criação de novas atividades/categorias,
 * atualização de realizações para o dia de hoje, e consulta da API de feriados via Ktor.
 *
 * @property activityRepository Repositório de dados das atividades e categorias (Room).
 * @property calendarRepository Repositório de integração com a API externa de calendário.
 */
class HomeViewModel(
    private val activityRepository: ActivityRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    /**
     * Estado reativo da UI combinando os fluxos de Atividades e Categorias do banco em tempo real.
     *
     * Permanece ativo enquanto houver inscritos na tela (com tolerância de 5 segundos ao girar a tela).
     */
    val uiState: StateFlow<HomeUiState> = combine(
        activityRepository.observeActivities(),
        activityRepository.observeCategories()
    ) { activities, categories ->
        HomeUiState.Success(activities, categories) as HomeUiState
    }
        .catch { emit(HomeUiState.Error("Não foi possível carregar as atividades.")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    // Estado interno do card do próximo feriado
    private val _holidayState = MutableStateFlow<HolidayUiState>(HolidayUiState.Loading)
    val holidayState: StateFlow<HolidayUiState> = _holidayState.asStateFlow()

    // Canal de eventos pontuais (mensagens de snackbar e notificações de sucesso/erro)
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    init {
        // Popula os dados iniciais do banco de dados na primeira execução
        viewModelScope.launch {
            activityRepository.seedInitialDataIfEmpty()
        }
        // Carrega o próximo feriado na API externa
        loadHoliday()
    }

    /**
     * Ponto de entrada para processamento de intenções do usuário (MVI/Event-driven).
     *
     * @param event O evento recebido da interface do usuário.
     */
    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.MarkToday -> markToday(event.activityId)
            is HomeEvent.Delete -> delete(event.activityId)
            is HomeEvent.AddActivity -> addActivity(event.name, event.date, event.category, event.notes)
            is HomeEvent.AddCategory -> addCategory(event.name)
            HomeEvent.RetryHoliday -> loadHoliday()
        }
    }

    /**
     * Cria uma nova categoria personalizada no repositório.
     */
    private fun addCategory(name: String) {
        viewModelScope.launch {
            activityRepository.addCategory(name).fold(
                onSuccess = { _messages.send("Categoria criada com sucesso.") },
                onFailure = { _messages.send(it.toUserMessage("Não foi possível criar a categoria.")) }
            )
        }
    }

    /**
     * Adiciona uma nova atividade com histórico inicial.
     */
    private fun addActivity(name: String, date: LocalDate, category: String, notes: String) {
        viewModelScope.launch {
            activityRepository.addActivity(name, date, category, notes).fold(
                onSuccess = { _messages.send("Atividade salva com sucesso.") },
                onFailure = { _messages.send(it.toUserMessage("Não foi possível salvar a atividade.")) }
            )
        }
    }

    /**
     * Registra a realização da atividade para o dia de hoje.
     */
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

    /**
     * Exclui uma atividade do banco de dados.
     */
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

    /**
     * Requisita o próximo feriado nacional utilizando o repositório de calendário.
     */
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
