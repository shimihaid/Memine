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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * ==============================================================================
 * VIEWMODEL MVVM MAIS SIMPLES DE TODAS
 * ==============================================================================
 * A ViewModel guarda a lista de atividades e categorias que a tela precisa.
 * A tela chama as funções diretamente quando o usuário clica nos botões!
 */
class HomeViewModel(
    private val activityRepository: ActivityRepository,
    private val calendarRepository: CalendarRepository
) : ViewModel() {

    // 1. Lista de atividades do banco de dados (A tela lê direto daqui!)
    val activities: StateFlow<List<Activity>> = activityRepository.observeActivities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // 2. Lista de categorias do banco de dados (A tela lê direto daqui!)
    val categories: StateFlow<List<String>> = activityRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Canal para avisos rápidos na tela (Snackbar)
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    init {
        // Inicializa o banco com dados de exemplo se estiver vazio
        viewModelScope.launch {
            activityRepository.seedInitialDataIfEmpty()
        }
    }

    // ==============================================================================
    // AÇÕES DA TELA (FUNÇÕES MVVM DIRETAS)
    // ==============================================================================

    /** Salva uma nova atividade */
    fun addActivity(name: String, date: LocalDate, category: String, notes: String) {
        viewModelScope.launch {
            try {
                activityRepository.addActivity(name, date, category, notes)
                _messages.send("Atividade salva com sucesso.")
            } catch (e: Exception) {
                _messages.send(e.message ?: "Erro ao salvar atividade.")
            }
        }
    }

    /** Cria uma nova categoria */
    fun addCategory(name: String) {
        viewModelScope.launch {
            try {
                activityRepository.addCategory(name)
                _messages.send("Categoria criada com sucesso.")
            } catch (e: Exception) {
                _messages.send(e.message ?: "Erro ao criar categoria.")
            }
        }
    }

    /** Marca a atividade como feita hoje */
    fun markToday(activityId: Long) {
        viewModelScope.launch {
            try {
                val outcome = activityRepository.markPerformedToday(activityId)
                val msg = when (outcome) {
                    MarkOutcome.MARKED -> "Atividade marcada como realizada hoje!"
                    MarkOutcome.ALREADY_MARKED_TODAY -> "Você já marcou essa atividade hoje."
                    MarkOutcome.NOT_FOUND -> "Atividade não encontrada."
                }
                _messages.send(msg)
            } catch (e: Exception) {
                _messages.send(e.message ?: "Erro ao atualizar atividade.")
            }
        }
    }

    /** Exclui uma atividade */
    fun deleteActivity(activityId: Long) {
        viewModelScope.launch {
            try {
                activityRepository.deleteActivity(activityId)
                _messages.send("Atividade excluída com sucesso.")
            } catch (e: Exception) {
                _messages.send(e.message ?: "Erro ao excluir atividade.")
            }
        }
    }
}
