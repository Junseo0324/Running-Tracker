package com.devhjs.runningtracker.presentation.result

sealed interface ResultAction {
    data class OnSaveClick(val bitmap: android.graphics.Bitmap?) : ResultAction
    data object OnDiscardClick : ResultAction
}
