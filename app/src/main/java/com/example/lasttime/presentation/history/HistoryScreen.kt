package com.example.lasttime.presentation.history

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lasttime.domain.model.KanbanTask
import com.example.lasttime.presentation.AppViewModelProvider
import com.example.lasttime.presentation.components.CenteredLoading
import com.example.lasttime.presentation.components.CenteredMessage
import com.example.lasttime.presentation.components.toBr
import com.example.lasttime.ui.theme.DarkText
import com.example.lasttime.ui.theme.ModalBorder
import com.example.lasttime.ui.theme.ModalButtonCancel
import com.example.lasttime.ui.theme.ModalButtonCancelText
import com.example.lasttime.ui.theme.PrimaryNavy
import com.example.lasttime.ui.theme.TabBorderDark
import com.example.lasttime.ui.theme.TabSelectedBg
import com.example.lasttime.ui.theme.TabUnselectedBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBackClick: (() -> Unit)? = null,
    viewModel: HistoryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var activeSubTab by remember { mutableStateOf("Kanban") } // "Kanban" ou "Histórico"
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Histórico & Kanban") },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            HistoryUiState.Loading -> CenteredLoading(Modifier.padding(padding))
            is HistoryUiState.Error -> CenteredMessage(state.message, Modifier.padding(padding))
            is HistoryUiState.Success -> {
                if (state.selected == null) {
                    CenteredMessage("Nenhuma atividade cadastrada.", Modifier.padding(padding))
                } else {
                    val activityId = state.selected.id

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = 600.dp)
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Carrossel de Atividades
                            LazyRow(
                                contentPadding = PaddingValues(vertical = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(state.activities, key = { it.id }) { activity ->
                                    FilterChip(
                                        selected = activity.id == state.selected.id,
                                        onClick = { viewModel.onSelectActivity(activity.id) },
                                        label = { Text(activity.name) }
                                    )
                                }
                            }

                            // Título da Atividade Selecionada
                            Text(
                                text = state.selected.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            // Seletor de Sub-Abas: [ Kanban de Tarefas ] | [ Histórico de Datas ]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(BorderStroke(1.5.dp, TabBorderDark)),
                                horizontalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                val tabs = listOf("Kanban", "Histórico de Datas")
                                tabs.forEachIndexed { index, tabName ->
                                    val isSelected = activeSubTab == tabName
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .background(if (isSelected) TabSelectedBg else TabUnselectedBg)
                                            .then(
                                                if (index > 0) Modifier.border(
                                                    BorderStroke(0.8.dp, TabBorderDark)
                                                ) else Modifier
                                            )
                                            .clickable { activeSubTab = tabName },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = tabName,
                                            color = DarkText,
                                            fontSize = 14.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(4.dp))

                            // CONTEÚDO DA ABA SELECIONADA
                            if (activeSubTab == "Kanban") {
                                // Botão Adicionar Sub-tarefa
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Sub-tarefas do Kanban",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    OutlinedButton(
                                        onClick = { showAddTaskDialog = true },
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text("Nova Tarefa")
                                    }
                                }

                                // Verifica se todas estão concluídas
                                val allCompleted = state.kanbanTasks.isNotEmpty() &&
                                        state.kanbanTasks.all { it.status == KanbanTask.STATUS_COMPLETED }

                                if (allCompleted) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "🎉 Todas as sub-tarefas foram concluídas! Atividade gravada como realizada hoje.",
                                            color = Color(0xFF2E7D32),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                }

                                // Exibição das 3 colunas do Kanban (Pendente, Em andamento, Concluído)
                                val statuses = listOf(
                                    KanbanTask.STATUS_PENDING to "📌 Pendente",
                                    KanbanTask.STATUS_IN_PROGRESS to "⏳ Em andamento",
                                    KanbanTask.STATUS_COMPLETED to "✅ Concluído"
                                )

                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {
                                    statuses.forEach { (statusKey, statusTitle) ->
                                        val tasksInStatus = state.kanbanTasks.filter { it.status == statusKey }

                                        item(key = "kanban_$statusKey") {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(6.dp),
                                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Text(
                                                        text = "$statusTitle (${tasksInStatus.size})",
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = PrimaryNavy
                                                    )
                                                    Spacer(Modifier.height(8.dp))

                                                    if (tasksInStatus.isEmpty()) {
                                                        Text(
                                                            text = "Nenhuma tarefa",
                                                            fontSize = 13.sp,
                                                            color = Color.Gray
                                                        )
                                                    } else {
                                                        tasksInStatus.forEach { task ->
                                                            KanbanTaskItemRow(
                                                                task = task,
                                                                onStatusChange = { newStatus ->
                                                                    viewModel.updateKanbanTaskStatus(task.id, activityId, newStatus)
                                                                },
                                                                onDelete = {
                                                                    viewModel.deleteKanbanTask(task.id)
                                                                }
                                                            )
                                                            Spacer(Modifier.height(6.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                // HISTÓRICO DE DATAS DE REALIZAÇÃO
                                Text(
                                    text = "${state.dates.size} registro(s) no histórico",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(bottom = 16.dp)
                                ) {
                                    items(state.dates) { date ->
                                        ListItem(headlineContent = { Text(date.toBr()) })
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }

                    // Modal para adicionar tarefa no Kanban
                    if (showAddTaskDialog) {
                        AddKanbanTaskDialog(
                            onDismiss = { showAddTaskDialog = false },
                            onSave = { taskTitle ->
                                viewModel.addKanbanTask(activityId, taskTitle)
                                showAddTaskDialog = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Componente que renderiza cada item dentro de uma coluna do Kanban.
 */
@Composable
private fun KanbanTaskItemRow(
    task: KanbanTask,
    onStatusChange: (String) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = task.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = DarkText,
                modifier = Modifier.weight(1f)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Ações dependendo do status atual
                when (task.status) {
                    KanbanTask.STATUS_PENDING -> {
                        Button(
                            onClick = { onStatusChange(KanbanTask.STATUS_IN_PROGRESS) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Iniciar", fontSize = 11.sp)
                        }
                    }
                    KanbanTask.STATUS_IN_PROGRESS -> {
                        Button(
                            onClick = { onStatusChange(KanbanTask.STATUS_COMPLETED) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Concluir", fontSize = 11.sp)
                        }
                    }
                    KanbanTask.STATUS_COMPLETED -> {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Concluído",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * Diálogo simples para inserção do título da nova sub-tarefa do Kanban.
 */
@Composable
private fun AddKanbanTaskDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color.White, shape = RoundedCornerShape(12.dp))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Nova Sub-tarefa do Kanban",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkText
                )

                Spacer(Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .border(BorderStroke(1.dp, ModalBorder), shape = RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (title.isEmpty()) {
                        Text("Título da sub-tarefa", color = Color(0xFF6E8192), fontSize = 15.sp)
                    }
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = TextStyle(color = DarkText, fontSize = 15.sp),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = ModalButtonCancel),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Cancelar", color = ModalButtonCancelText, fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) onSave(title.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNavy),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text("Adicionar", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
