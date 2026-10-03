package com.devhjs.runningtracker.presentation.history

sealed interface RunHistoryAction {
    data object OnBackClick: RunHistoryAction
    data class OnCoupangClick(val url: String): RunHistoryAction
    data class OnRunClick(val runId: Int): RunHistoryAction
}
