package com.example.lasttime.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lasttime.domain.model.Activity
import com.example.lasttime.presentation.AppViewModelProvider
import com.example.lasttime.presentation.components.ActivityCard
import com.example.lasttime.presentation.components.CenteredLoading
import com.example.lasttime.presentation.components.CenteredMessage
import com.example.lasttime.presentation.components.toBr

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddClick: () -> Unit,
    onActivityClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val holidayState by viewModel.holidayState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var activityToDelete by remember { mutableStateOf<Activity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text("Last Time") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar atividade")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            when (val state = uiState) {
                HomeUiState.Loading -> CenteredLoading()
                is HomeUiState.Error -> CenteredMessage(state.message)
                is HomeUiState.Success -> LazyColumn(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.activities.isEmpty()) {
                        item(key = "empty") {
                            Text(
                                text = "Nenhuma atividade ainda. Toque em + para adicionar.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp)
                            )
                        }
                    }
                    items(state.activities, key = { it.id }) { activity ->
                        ActivityCard(
                            activity = activity,
                            onClick = { onActivityClick(activity.id) },
                            onMarkToday = { viewModel.onEvent(HomeEvent.MarkToday(activity.id)) },
                            onDelete = { activityToDelete = activity }
                        )
                    }
                    item(key = "holiday") {
                        HolidayCard(
                            state = holidayState,
                            onRetry = { viewModel.onEvent(HomeEvent.RetryHoliday) }
                        )
                    }
                }
            }
        }
    }

    activityToDelete?.let { activity ->
        AlertDialog(
            onDismissRequest = { activityToDelete = null },
            title = { Text("Excluir atividade?") },
            text = { Text("\"${activity.name}\" e todo o histórico dela serão apagados.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onEvent(HomeEvent.Delete(activity.id))
                    activityToDelete = null
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { activityToDelete = null }) { Text("Cancelar") }
            }
        )
    }
}

/** Informação complementar da API: se falhar, só esta caixa mostra o aviso. */
@Composable
private fun HolidayCard(state: HolidayUiState, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text(
                text = when (state) {
                    HolidayUiState.Loading -> "Buscando próximo feriado…"
                    is HolidayUiState.Loaded -> state.holiday
                        ?.let { "Próximo feriado: ${it.date.toBr()} — ${it.name}" }
                        ?: "Nenhum feriado encontrado."
                    is HolidayUiState.Error -> state.message
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            if (state is HolidayUiState.Error) {
                TextButton(onClick = onRetry) { Text("Tentar de novo") }
            }
        }
    }
}