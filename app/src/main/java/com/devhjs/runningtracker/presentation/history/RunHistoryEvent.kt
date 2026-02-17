package com.devhjs.runningtracker.presentation.history

sealed interface RunHistoryEvent {
    data object NavigateUp: RunHistoryEvent
}
