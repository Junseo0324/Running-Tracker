package com.devhjs.runningtracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devhjs.runningtracker.presentation.home.HomeScreenRoot
import com.devhjs.runningtracker.presentation.run.ResultScreen
import com.devhjs.runningtracker.presentation.run.RunScreen

@Composable
fun Navigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.HomeScreen.route
    ) {
        composable(Screen.HomeScreen.route) {
            HomeScreenRoot(
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(Screen.RunScreen.route) {
            RunScreen(navController = navController)
        }
        composable(Screen.ResultScreen.route) {
            ResultScreen(navController = navController)
        }
    }
}
