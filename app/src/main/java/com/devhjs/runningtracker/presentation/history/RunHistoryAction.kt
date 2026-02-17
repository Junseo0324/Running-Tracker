package com.devhjs.runningtracker.presentation.history

sealed interface RunHistoryAction {
    data object OnBackClick: RunHistoryAction
}
