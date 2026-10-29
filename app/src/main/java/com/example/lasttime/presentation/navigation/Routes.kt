package com.example.lasttime.presentation.navigation

object Routes {
    const val ARG_ACTIVITY_ID = "activityId"

    const val HOME = "home"
    const val CALENDAR = "calendar"
    const val ADD = "add"

    /** "Histórico" abre na primeira atividade. */
    const val HISTORY = "history"

    /** Aberta ao tocar num card da Home: já vem com a atividade selecionada. */
    const val HISTORY_DETAIL = "history/{$ARG_ACTIVITY_ID}"

    fun historyOf(activityId: Long) = "history/$activityId"
}
