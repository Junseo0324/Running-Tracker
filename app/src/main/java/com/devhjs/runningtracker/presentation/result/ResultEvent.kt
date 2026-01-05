package com.devhjs.runningtracker.presentation.result

sealed interface ResultEvent {
    data class Navigate(val route: String) : ResultEvent
    data class StopService(val action: String) : ResultEvent
}
