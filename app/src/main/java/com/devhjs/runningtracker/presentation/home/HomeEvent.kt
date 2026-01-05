package com.devhjs.runningtracker.presentation.home

sealed interface HomeEvent {
    data class Navigate(val route: String): HomeEvent
}
