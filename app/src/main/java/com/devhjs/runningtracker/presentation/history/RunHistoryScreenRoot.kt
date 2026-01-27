package com.devhjs.runningtracker.presentation.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun RunHistoryScreenRoot(
    viewModel: RunHistoryViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val runs by viewModel.runs.collectAsStateWithLifecycle()
    RunHistoryScreen(
        runs = runs,
        onNavigateUp = onNavigateUp
    )
}
