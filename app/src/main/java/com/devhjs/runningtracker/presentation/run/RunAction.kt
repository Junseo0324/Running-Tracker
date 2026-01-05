package com.devhjs.runningtracker.presentation.run

sealed interface RunAction {
    data object OnToggleLock: RunAction
    data object OnResume: RunAction
    data object OnPause: RunAction
    data object OnFinish: RunAction
}
