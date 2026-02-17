package com.devhjs.runningtracker.presentation.history

import androidx.compose.runtime.Stable
import com.devhjs.runningtracker.domain.model.Run

@Stable
data class RunHistoryState(
    val runs: List<Run> = emptyList()
)
