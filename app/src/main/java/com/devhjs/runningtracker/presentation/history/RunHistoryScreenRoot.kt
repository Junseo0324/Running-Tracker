package com.devhjs.runningtracker.presentation.history

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devhjs.runningtracker.presentation.util.AdHelper

@Composable
fun RunHistoryScreenRoot(
    viewModel: RunHistoryViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        AdHelper.showInterstitial(context)
    }

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when(event) {
                RunHistoryEvent.NavigateUp -> onNavigateUp()
            }
        }
    }

    RunHistoryScreen(
        state = state,
        onAction = viewModel::onAction
    )
}
