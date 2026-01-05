package com.devhjs.runningtracker.presentation.navigation

sealed class Screen(val route: String) {
    object HomeScreen : Screen("home_screen")
    object RunScreen : Screen("run_screen")
    object StatisticsScreen : Screen("statistics_screen")
    object SettingsScreen : Screen("settings_screen")
    object SetupScreen : Screen("setup_screen")
    object ResultScreen : Screen("result_screen")
}
