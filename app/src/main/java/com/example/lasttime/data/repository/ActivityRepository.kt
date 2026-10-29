package com.example.lasttime.data.repository

import com.example.lasttime.data.local.dao.ActivityDao
import com.example.lasttime.data.local.entity.ActivityEntity
import com.example.lasttime.domain.model.Activity
import com.example.lasttime.domain.model.ActivityNotFoundException
import com.example.lasttime.domain.model.ActivityRules
import com.example.lasttime.domain.model.InvalidActivityException
import com.example.lasttime.domain.model.MarkOutcome
import com.example.lasttime.domain.model.calculateDaysSince
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class ActivityRepository(private val dao: ActivityDao) {

    fun observeActivities(): Flow<List<Activity>> =
        dao.observeActivities().map { list ->
            val today = LocalDate.now()
            list.map { it.toDomain(today) }
        }

    fun observeHistory(activityId: Long): Flow<List<LocalDate>> =
        dao.observeHistory(activityId).map { list -> list.map { it.performedAt } }

    suspend fun addActivity(
        name: String,
        performedAt: LocalDate,
        category: String = "Trabalho",
        notes: String = ""
    ): Result<Long> = safeCall {
        ActivityRules.validateName(name)?.let { throw InvalidActivityException(it) }
        if (performedAt.isAfter(LocalDate.now())) {
            throw InvalidActivityException("A data não pode estar no futuro.")
        }
        dao.insertActivityWithHistory(name.trim(), performedAt, category, notes)
    }

    suspend fun markPerformedToday(activityId: Long): Result<MarkOutcome> = safeCall {
        val outcome = dao.markPerformed(activityId, LocalDate.now())
        if (outcome == MarkOutcome.NOT_FOUND) throw ActivityNotFoundException()
        outcome
    }

    suspend fun deleteActivity(activityId: Long): Result<Unit> = safeCall {
        if (dao.deleteActivity(activityId) == 0) throw ActivityNotFoundException()
    }

    suspend fun seedInitialDataIfEmpty() {
        if (dao.countActivities() == 0) {
            val today = LocalDate.now()
            // Inicializa com as tarefas dos prints se o banco estiver vazio
            // Trabalho
            dao.insertActivityWithHistory("Atualizar a planilha semanal", today.minusDays(6), "Trabalho", "")
            dao.insertActivityWithHistory("Fazer o pagamento mensal", today.minusDays(15), "Trabalho", "")
            // Pessoal
            dao.insertActivityWithHistory("Cortar o cabelo", today.minusDays(60), "Pessoal", "")
            dao.insertActivityWithHistory("Lavar o carro", today.minusDays(60), "Pessoal", "")
            // Plantas
            dao.insertActivityWithHistory("Regar a suculenta", today.minusDays(7), "Plantas", "")
            dao.insertActivityWithHistory("Podar a roseira", today.minusDays(90), "Plantas", "")
        }
    }

    private fun ActivityEntity.toDomain(today: LocalDate) = Activity(
        id = id,
        name = name,
        lastPerformedAt = lastPerformedAt,
        daysSinceLastPerformed = calculateDaysSince(lastPerformedAt, today),
        category = category,
        notes = notes
    )
}
