package com.devhjs.runningtracker.presentation.result

sealed interface ResultAction {
    data object OnSaveClick : ResultAction
    data object OnDiscardClick : ResultAction
}
