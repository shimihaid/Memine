package com.example.lasttime.presentation.components

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val brFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")

fun LocalDate.toBr(): String = format(brFormatter)

// O DatePicker do Material 3 trabalha com milissegundos em UTC.
fun LocalDate.toUtcMillis(): Long = atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

fun Long.toLocalDateFromUtcMillis(): LocalDate =
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

fun daysLabel(days: Long): String = when {
    days == 0L -> "Hoje"
    days == 1L -> "Há 1 dia"
    days in 2L..6L -> "Há $days dias"
    days in 7L..13L -> "Há 1 semana"
    days in 14L..29L -> "Há $days dias"
    days in 30L..59L -> "Há 1 mês"
    days in 60L..364L -> {
        val months = days / 30
        "Há $months meses"
    }
    days in 365L..729L -> "Há 1 ano"
    else -> {
        val years = days / 365
        "Há $years anos"
    }
}
