package com.devhjs.runningtracker.presentation.history

sealed interface RunHistoryEvent {
    data object NavigateUp: RunHistoryEvent
    data class OpenUrl(val url: String): RunHistoryEvent
}
