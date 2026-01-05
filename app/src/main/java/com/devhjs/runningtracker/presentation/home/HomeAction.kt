package com.devhjs.runningtracker.presentation.home


sealed interface HomeAction {
    data class OnPermissionsResult(val isGranted: Boolean): HomeAction
    object OnStartClick: HomeAction
}
