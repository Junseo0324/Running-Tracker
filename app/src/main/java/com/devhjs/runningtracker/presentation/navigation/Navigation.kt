package com.devhjs.runningtracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.devhjs.runningtracker.presentation.home.HomeScreenRoot
import com.devhjs.runningtracker.presentation.result.ResultScreenRoot
import com.devhjs.runningtracker.presentation.run.RunScreenRoot

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
            RunScreenRoot(
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(Screen.ResultScreen.route) {
            ResultScreenRoot(
                onNavigate = { route -> navController.navigate(route) }
            )
        }
    }
}
