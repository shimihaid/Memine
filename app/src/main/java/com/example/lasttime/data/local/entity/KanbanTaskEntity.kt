package com.example.lasttime.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "kanban_tasks",
    foreignKeys = [
        ForeignKey(
            entity = ActivityEntity::class,
            parentColumns = ["id"],
            childColumns = ["activityId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("activityId")]
)
data class KanbanTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activityId: Long,
    val title: String,
    val status: String = "Pendente"
)
