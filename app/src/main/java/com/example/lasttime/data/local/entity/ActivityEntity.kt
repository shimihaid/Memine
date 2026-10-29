package com.example.lasttime.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val lastPerformedAt: LocalDate,
    val category: String = "Trabalho",
    val notes: String = ""
)
