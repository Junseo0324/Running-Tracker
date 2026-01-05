package com.devhjs.runningtracker.presentation.result

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devhjs.runningtracker.service.TrackingService

@Composable
fun ResultScreenRoot(
    viewModel: ResultViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(true) {
        viewModel.event.collect { event ->
            when(event) {
                is ResultEvent.Navigate -> {
                    onNavigate(event.route)
                }
                is ResultEvent.StopService -> {
                    Intent(context, TrackingService::class.java).also {
                        it.action = event.action
                        context.startService(it)
                    }
                }
            }
        }
    }

    ResultScreen(
        state = state,
        onAction = viewModel::onAction
    )
}
