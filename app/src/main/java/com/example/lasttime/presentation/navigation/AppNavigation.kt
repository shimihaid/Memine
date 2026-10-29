package com.example.lasttime.presentation.navigation

// Importações do Jetpack Compose Layout e Scaffold
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

// Importações de estado e reatividade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

// Componentes do Jetpack Navigation Compose
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// Componentes das telas e fábrica do ViewModel
import com.example.lasttime.presentation.AppViewModelProvider
import com.example.lasttime.presentation.calendar.CalendarScreen
import com.example.lasttime.presentation.components.AddActivityDialog
import com.example.lasttime.presentation.components.MemineBottomBar
import com.example.lasttime.presentation.components.MemineTab
import com.example.lasttime.presentation.history.HistoryScreen
import com.example.lasttime.presentation.home.HomeEvent
import com.example.lasttime.presentation.home.HomeScreen
import com.example.lasttime.presentation.home.HomeUiState
import com.example.lasttime.presentation.home.HomeViewModel
import com.example.lasttime.ui.theme.AppBackground

/**
 * Componente principal de navegação do aplicativo.
 *
 * Configura o [NavHost] com as rotas principais (Home, Calendário, Histórico),
 * a barra de navegação inferior [MemineBottomBar] e o modal flutuante de criação de atividade.
 *
 * @param modifier Modificador de layout.
 * @param homeViewModel Instância compartilhada do [HomeViewModel] inicializado pela fábrica central.
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    // Controlador central da pilha de navegação do Compose
    val navController = rememberNavController()

    // Observa a entrada atual do backstack para determinar a rota ativa
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    // Coleta as categorias atualizadas da HomeViewModel para alimentar o modal de nova atividade
    val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val availableCategories = when (val state = homeUiState) {
        is HomeUiState.Success -> state.categories.ifEmpty { listOf("Trabalho", "Pessoal", "Plantas") }
        else -> listOf("Trabalho", "Pessoal", "Plantas")
    }

    // Controle de exibição do diálogo de criação de atividade
    var showAddDialog by remember { mutableStateOf(false) }

    // Estado do host de mensagens curtas (Snackbar)
    val snackbarHostState = remember { SnackbarHostState() }

    // Coleta mensagens enviadas pelo canal da ViewModel e as exibe via Snackbar
    LaunchedEffect(Unit) {
        homeViewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    // Identifica qual aba da barra inferior deve ficar destacada
    val currentTab = when (route) {
        Routes.CALENDAR -> MemineTab.CALENDARIO
        else -> MemineTab.ATIVIDADES
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
        contentWindowInsets = WindowInsets.statusBars,
        // Barra de navegação inferior personalizada
        bottomBar = {
            MemineBottomBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    when (tab) {
                        MemineTab.ATIVIDADES -> navController.navigateToTab(Routes.HOME)
                        MemineTab.CALENDARIO -> navController.navigateToTab(Routes.CALENDAR)
                    }
                },
                onAddClick = { showAddDialog = true } // Botão "+" central abre o modal
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppBackground
    ) { innerPadding ->
        // Container principal que consome os insets de janela do Scaffold
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            // Grafo de navegação das telas da aplicação
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.fillMaxSize()
            ) {
                // Rota da Tela Inicial (Home)
                composable(Routes.HOME) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onActivityClick = { id ->
                            // Navega para o histórico da atividade selecionada
                            navController.navigate(Routes.historyOf(id)) { launchSingleTop = true }
                        }
                    )
                }

                // Rota da Tela de Calendário
                composable(Routes.CALENDAR) {
                    CalendarScreen(
                        onDayClick = {
                            // Handler de clique em dia do calendário
                        }
                    )
                }

                // Rota padrão do Histórico (sem parâmetro inicial)
                composable(Routes.HISTORY) {
                    HistoryScreen(
                        onBackClick = if (navController.previousBackStackEntry != null) {
                            { navController.popBackStack() }
                        } else null
                    )
                }

                // Rota detalhada do Histórico para uma atividade específica
                composable(
                    route = Routes.HISTORY_DETAIL,
                    arguments = listOf(navArgument(Routes.ARG_ACTIVITY_ID) { type = NavType.LongType })
                ) {
                    HistoryScreen(
                        onBackClick = { navController.popBackStack() } // Fornece a ação de voltar
                    )
                }
            }

            // Modal / Dialog Nova Atividade
            if (showAddDialog) {
                AddActivityDialog(
                    onDismiss = { showAddDialog = false },
                    availableCategories = availableCategories,
                    onAddCategory = { newCategory ->
                        homeViewModel.onEvent(HomeEvent.AddCategory(newCategory))
                    },
                    onSave = { name, date, category, notes ->
                        homeViewModel.onEvent(
                            HomeEvent.AddActivity(
                                name = name,
                                date = date,
                                category = category,
                                notes = notes
                            )
                        )
                        showAddDialog = false
                    }
                )
            }
        }
    }
}

/**
 * Função utilitária para troca suave entre abas mantendo a pilha limpa.
 *
 * @param route Nome da rota de destino.
 */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
