package com.devhjs.runningtracker.presentation.home

sealed interface HomeEvent {
    data class Navigate(val route: String): HomeEvent
    data class ShowBatteryLowWarning(val message: String): HomeEvent
}
