package com.example.lasttime.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.lasttime.presentation.addactivity.AddActivityScreen
import com.example.lasttime.presentation.history.HistoryScreen
import com.example.lasttime.presentation.home.HomeScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route

    val isHome = route == Routes.HOME
    val isHistory = route == Routes.HISTORY || route == Routes.HISTORY_DETAIL

    Scaffold(
        modifier = modifier,
       
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // Na tela de adicionar não há Bottom Navigation
            if (route != Routes.ADD) {
                NavigationBar {
                    NavigationBarItem(
                        selected = isHome,
                        onClick = { if (!isHome) navController.navigateToTab(Routes.HOME) },
                        icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                        label = { Text("Home") }
                    )
                    NavigationBarItem(
                        selected = isHistory,
                        onClick = { if (!isHistory) navController.navigateToTab(Routes.HISTORY) },
                        icon = { Icon(Icons.Filled.History, contentDescription = null) },
                        label = { Text("Histórico") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    onAddClick = { navController.navigate(Routes.ADD) },
                    onActivityClick = { id ->
                        navController.navigate(Routes.historyOf(id)) { launchSingleTop = true }
                    }
                )
            }
            composable(Routes.ADD) {
                AddActivityScreen(onBack = { navController.popBackStack() })
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
    }
}

/** Troca de aba: volta até a Home e evita empilhar a mesma tela duas vezes. */
private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { inclusive = false }
        launchSingleTop = true
    }
}