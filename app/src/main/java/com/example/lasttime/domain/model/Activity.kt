package com.example.lasttime.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Activity(
    val id: Long,
    val name: String,
    val lastPerformedAt: LocalDate,
    val daysSinceLastPerformed: Long,
    val category: String = "Trabalho",
    val notes: String = ""
)

/** Regra principal: dias = hoje - última realização (só datas, sem hora). */
fun calculateDaysSince(lastPerformedAt: LocalDate, today: LocalDate): Long =
    ChronoUnit.DAYS.between(lastPerformedAt, today).coerceAtLeast(0)
