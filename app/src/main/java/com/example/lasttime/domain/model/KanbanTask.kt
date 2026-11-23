package com.example.lasttime.domain.model

data class KanbanTask(
    val id: Long = 0,
    val activityId: Long,
    val title: String,
    val status: String = STATUS_PENDING
) {
    companion object {
        const val STATUS_PENDING = "Pendente"
        const val STATUS_IN_PROGRESS = "Em andamento"
        const val STATUS_COMPLETED = "Concluído"
    }
}
