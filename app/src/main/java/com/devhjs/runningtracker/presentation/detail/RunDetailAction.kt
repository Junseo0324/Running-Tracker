package com.devhjs.runningtracker.presentation.detail

sealed interface RunDetailAction {
    data object OnBackClick : RunDetailAction
    data object OnDeleteClick : RunDetailAction
    data object OnDeleteConfirm : RunDetailAction
    data object OnDeleteDismiss : RunDetailAction
    data class OnCoupangClick(val url: String) : RunDetailAction
}
