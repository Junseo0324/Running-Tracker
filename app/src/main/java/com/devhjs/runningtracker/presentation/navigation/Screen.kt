package com.devhjs.runningtracker.presentation.navigation

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home_screen")
    object RunScreen : Screen("run_screen")
    object ResultScreen : Screen("result_screen")
    object RunHistoryScreen : Screen("run_history_screen")
}
