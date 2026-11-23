package com.example.lasttime.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.lasttime.data.local.entity.KanbanTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KanbanTaskDao {
    @Query("SELECT * FROM kanban_tasks WHERE activityId = :activityId ORDER BY id ASC")
    fun observeTasksForActivity(activityId: Long): Flow<List<KanbanTaskEntity>>

    @Query("SELECT * FROM kanban_tasks WHERE activityId = :activityId")
    suspend fun getTasksForActivity(activityId: Long): List<KanbanTaskEntity>

    @Insert
    suspend fun insertTask(task: KanbanTaskEntity): Long

    @Update
    suspend fun updateTask(task: KanbanTaskEntity): Int

    @Query("UPDATE kanban_tasks SET status = :status WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: String): Int

    @Query("DELETE FROM kanban_tasks WHERE id = :taskId")
    suspend fun deleteTask(taskId: Long): Int
}
