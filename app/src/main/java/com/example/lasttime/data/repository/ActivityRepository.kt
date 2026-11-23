package com.example.lasttime.data.repository

import com.example.lasttime.data.local.dao.ActivityDao
import com.example.lasttime.data.local.dao.CategoryDao
import com.example.lasttime.data.local.dao.KanbanTaskDao
import com.example.lasttime.data.local.entity.ActivityEntity
import com.example.lasttime.data.local.entity.CategoryEntity
import com.example.lasttime.data.local.entity.KanbanTaskEntity
import com.example.lasttime.domain.model.Activity
import com.example.lasttime.domain.model.ActivityRules
import com.example.lasttime.domain.model.KanbanTask
import com.example.lasttime.domain.model.MarkOutcome
import com.example.lasttime.domain.model.calculateDaysSince
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * ==============================================================================
 * REPOSITÓRIO SIMPLES COM SUPORTE A KANBAN
 * ==============================================================================
 * O Repositório é a única ponte entre o Banco de Dados (Room) e a ViewModel.
 */
class ActivityRepository(
    private val dao: ActivityDao,
    private val categoryDao: CategoryDao,
    private val kanbanTaskDao: KanbanTaskDao
) {

    /** Retorna o fluxo contínuo de atividades salvas no banco de dados */
    fun observeActivities(): Flow<List<Activity>> =
        dao.observeActivities().map { list ->
            val today = LocalDate.now()
            list.map { it.toDomain(today) }
        }

    /** Retorna o fluxo contínuo com os nomes de todas as categorias */
    fun observeCategories(): Flow<List<String>> =
        categoryDao.observeCategories().map { list -> list.map { it.name } }

    /** Retorna o histórico de datas de uma atividade específica */
    fun observeHistory(activityId: Long): Flow<List<LocalDate>> =
        dao.observeHistory(activityId).map { list -> list.map { it.performedAt } }

    // ==============================================================================
    // OPERAÇÕES DO KANBAN DE SUB-TAREFAS
    // ==============================================================================

    /** Observa as tarefas do Kanban de uma atividade em tempo real */
    fun observeKanbanTasks(activityId: Long): Flow<List<KanbanTask>> =
        kanbanTaskDao.observeTasksForActivity(activityId).map { list ->
            list.map { KanbanTask(it.id, it.activityId, it.title, it.status) }
        }

    /** Adiciona uma nova sub-tarefa no Kanban da atividade */
    suspend fun addKanbanTask(activityId: Long, title: String): Long {
        val trimmed = title.trim()
        if (trimmed.isBlank()) throw Exception("O nome da tarefa não pode ser vazio.")
        return kanbanTaskDao.insertTask(
            KanbanTaskEntity(
                activityId = activityId,
                title = trimmed,
                status = KanbanTask.STATUS_PENDING
            )
        )
    }

    /**
     * Atualiza o status de uma sub-tarefa do Kanban ("Pendente", "Em andamento", "Concluído").
     * SE TODAS as sub-tarefas da atividade forem concluídas, marca a atividade principal como realizada HOJE!
     * @return true se a atividade principal foi salva como realizada hoje automaticamente.
     */
    suspend fun updateKanbanTaskStatus(taskId: Long, activityId: Long, newStatus: String): Boolean {
        kanbanTaskDao.updateTaskStatus(taskId, newStatus)

        // Verifica se TODAS as sub-tarefas desta atividade estão concluídas
        val allTasks = kanbanTaskDao.getTasksForActivity(activityId)
        val allCompleted = allTasks.isNotEmpty() && allTasks.all { it.status == KanbanTask.STATUS_COMPLETED }

        if (allCompleted) {
            // Marca automaticamente a atividade principal como realizada no dia de HOJE
            dao.markPerformed(activityId, LocalDate.now())
            return true
        }
        return false
    }

    /** Exclui uma sub-tarefa do Kanban pelo ID */
    suspend fun deleteKanbanTask(taskId: Long) {
        kanbanTaskDao.deleteTask(taskId)
    }

    // ==============================================================================
    // OPERAÇÕES DE ATIVIDADES E CATEGORIAS
    // ==============================================================================

    /** Adiciona uma nova categoria no banco de dados */
    suspend fun addCategory(name: String) {
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            throw Exception("O nome da categoria não pode ser vazio.")
        }
        if (categoryDao.countCategoryByName(trimmed) > 0) {
            throw Exception("Essa categoria já existe.")
        }
        categoryDao.insertCategory(CategoryEntity(name = trimmed))
    }

    /** Salva uma nova atividade no banco de dados */
    suspend fun addActivity(
        name: String,
        performedAt: LocalDate,
        category: String = "Trabalho",
        notes: String = ""
    ): Long {
        ActivityRules.validateName(name)?.let { throw Exception(it) }
        if (performedAt.isAfter(LocalDate.now())) {
            throw Exception("A data não pode estar no futuro.")
        }
        return dao.insertActivityWithHistory(name.trim(), performedAt, category, notes)
    }

    /** Marca que a atividade foi realizada no dia de hoje */
    suspend fun markPerformedToday(activityId: Long): MarkOutcome {
        val outcome = dao.markPerformed(activityId, LocalDate.now())
        if (outcome == MarkOutcome.NOT_FOUND) {
            throw Exception("Atividade não encontrada.")
        }
        return outcome
    }

    /** Exclui uma atividade pelo ID */
    suspend fun deleteActivity(activityId: Long) {
        if (dao.deleteActivity(activityId) == 0) {
            throw Exception("Atividade não encontrada.")
        }
    }

    /** Popula o banco com dados de exemplo se estiver vazio na primeira abertura */
    suspend fun seedInitialDataIfEmpty() {
        if (categoryDao.countCategories() == 0) {
            categoryDao.insertCategory(CategoryEntity(name = "Trabalho"))
            categoryDao.insertCategory(CategoryEntity(name = "Pessoal"))
            categoryDao.insertCategory(CategoryEntity(name = "Plantas"))
        }

        if (dao.countActivities() == 0) {
            val today = LocalDate.now()
            val id1 = dao.insertActivityWithHistory("Atualizar a planilha semanal", today.minusDays(6), "Trabalho", "")
            dao.insertActivityWithHistory("Fazer o pagamento mensal", today.minusDays(15), "Trabalho", "")
            dao.insertActivityWithHistory("Cortar o cabelo", today.minusDays(60), "Pessoal", "")
            val id2 = dao.insertActivityWithHistory("Lavar o carro", today.minusDays(60), "Pessoal", "")
            dao.insertActivityWithHistory("Regar a suculenta", today.minusDays(7), "Plantas", "")
            dao.insertActivityWithHistory("Podar a roseira", today.minusDays(90), "Plantas", "")

            // Exemplo de Kanban inicial para a atividade 1
            kanbanTaskDao.insertTask(KanbanTaskEntity(activityId = id1, title = "Conferir extrato", status = "Pendente"))
            kanbanTaskDao.insertTask(KanbanTaskEntity(activityId = id1, title = "Preencher dados de gastos", status = "Em andamento"))

            // Exemplo de Kanban inicial para a atividade de Lavar o Carro
            kanbanTaskDao.insertTask(KanbanTaskEntity(activityId = id2, title = "Aspirar os bancos", status = "Pendente"))
            kanbanTaskDao.insertTask(KanbanTaskEntity(activityId = id2, title = "Enxaguar a lataria", status = "Pendente"))
        }
    }

    /** Converte a classe do banco (ActivityEntity) para a classe da tela (Activity) */
    private fun ActivityEntity.toDomain(today: LocalDate) = Activity(
        id = id,
        name = name,
        lastPerformedAt = lastPerformedAt,
        daysSinceLastPerformed = calculateDaysSince(lastPerformedAt, today),
        category = category,
        notes = notes
    )
}
