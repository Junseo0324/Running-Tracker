package com.devhjs.runningtracker.presentation.run

sealed interface RunEvent {
    data class Navigate(val route: String): RunEvent
    data class ServiceAction(val action: String): RunEvent
}
