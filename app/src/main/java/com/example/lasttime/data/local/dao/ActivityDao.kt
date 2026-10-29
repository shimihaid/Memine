package com.example.lasttime.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.lasttime.data.local.entity.ActivityEntity
import com.example.lasttime.data.local.entity.ActivityHistoryEntity
import com.example.lasttime.domain.model.MarkOutcome
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
abstract class ActivityDao {

    // ---------- atividades ----------
    @Insert
    abstract suspend fun insertActivity(activity: ActivityEntity): Long

    // Mais tempo sem fazer aparece primeiro
    @Query("SELECT * FROM activities ORDER BY lastPerformedAt ASC, name ASC")
    abstract fun observeActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id")
    abstract suspend fun getActivityById(id: Long): ActivityEntity?

    @Query("UPDATE activities SET lastPerformedAt = :date WHERE id = :id")
    abstract suspend fun updateLastPerformed(id: Long, date: LocalDate): Int

    @Query("DELETE FROM activities WHERE id = :id")
    abstract suspend fun deleteActivity(id: Long): Int

    @Query("SELECT COUNT(*) FROM activities")
    abstract suspend fun countActivities(): Int

    // ---------- histórico ----------
    @Insert
    abstract suspend fun insertHistory(entry: ActivityHistoryEntity): Long

    @Query("SELECT * FROM activity_history WHERE activityId = :activityId ORDER BY performedAt DESC, id DESC")
    abstract fun observeHistory(activityId: Long): Flow<List<ActivityHistoryEntity>>

    @Query("SELECT COUNT(*) FROM activity_history WHERE activityId = :activityId AND performedAt = :date")
    abstract suspend fun countHistoryOn(activityId: Long, date: LocalDate): Int

    // ---------- operações compostas (transação) ----------
    /** Cria a atividade e o primeiro registro do histórico de forma atômica. */
    @Transaction
    open suspend fun insertActivityWithHistory(
        name: String,
        date: LocalDate,
        category: String = "Trabalho",
        notes: String = ""
    ): Long {
        val id = insertActivity(
            ActivityEntity(
                name = name,
                lastPerformedAt = date,
                category = category,
                notes = notes
            )
        )
        insertHistory(ActivityHistoryEntity(activityId = id, performedAt = date))
        return id
    }

    /** "Hoje": cria o registro no histórico e atualiza lastPerformedAt, sem duplicar. */
    @Transaction
    open suspend fun markPerformed(id: Long, date: LocalDate): MarkOutcome {
        if (getActivityById(id) == null) return MarkOutcome.NOT_FOUND
        if (countHistoryOn(id, date) > 0) return MarkOutcome.ALREADY_MARKED_TODAY
        insertHistory(ActivityHistoryEntity(activityId = id, performedAt = date))
        updateLastPerformed(id, date)
        return MarkOutcome.MARKED
    }
}
