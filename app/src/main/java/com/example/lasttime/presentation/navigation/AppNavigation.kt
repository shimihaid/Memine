package com.example.lasttime.presentation.navigation

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lasttime.presentation.AppViewModelProvider
import com.example.lasttime.presentation.calendar.CalendarScreen
import com.example.lasttime.presentation.components.AddActivityDialog
import com.example.lasttime.presentation.components.MemineBottomBar
import com.example.lasttime.presentation.components.MemineTab
import com.example.lasttime.presentation.history.HistoryScreen
import com.example.lasttime.presentation.home.HomeEvent
import com.example.lasttime.presentation.home.HomeScreen
import com.example.lasttime.presentation.home.HomeViewModel
import com.example.lasttime.ui.theme.AppBackground

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        homeViewModel.messages.collect { snackbarHostState.showSnackbar(it) }
    }

    val currentTab = when (route) {
        Routes.CALENDAR -> MemineTab.CALENDARIO
        else -> MemineTab.ATIVIDADES
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground),
        contentWindowInsets = WindowInsets.statusBars,
        bottomBar = {
            MemineBottomBar(
                currentTab = currentTab,
                onTabSelected = { tab ->
                    when (tab) {
                        MemineTab.ATIVIDADES -> navController.navigateToTab(Routes.HOME)
                        MemineTab.CALENDARIO -> navController.navigateToTab(Routes.CALENDAR)
                    }
                },
                onAddClick = { showAddDialog = true }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = AppBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier.fillMaxSize()
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        onActivityClick = { id ->
                            navController.navigate(Routes.historyOf(id)) { launchSingleTop = true }
                        }
                    )
                }

                composable(Routes.CALENDAR) {
                    CalendarScreen(
                        onDayClick = {
                            // Dia selecionado no calendário
                        }
                    )
                }

                composable(Routes.HISTORY) {
                    HistoryScreen()
                }

                composable(
                    route = Routes.HISTORY_DETAIL,
                    arguments = listOf(navArgument(Routes.ARG_ACTIVITY_ID) { type = NavType.LongType })
                ) {
                    HistoryScreen()
                }
            }

            // Modal / Dialog Nova Atividade (Print 3)
            if (showAddDialog) {
                AddActivityDialog(
                    onDismiss = { showAddDialog = false },
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

/** Troca de aba mantendo pilha limpa. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
