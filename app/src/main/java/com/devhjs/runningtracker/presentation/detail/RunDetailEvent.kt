package com.devhjs.runningtracker.presentation.detail

sealed interface RunDetailEvent {
    data object NavigateUp : RunDetailEvent
    data class OpenUrl(val url: String) : RunDetailEvent
}
