package com.devhjs.runningtracker.presentation.result

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devhjs.runningtracker.presentation.util.AdHelper
import com.devhjs.runningtracker.presentation.util.SystemBarIcons
import com.devhjs.runningtracker.presentation.util.openExternalUrl
import com.devhjs.runningtracker.service.TrackingService

@Composable
fun ResultScreenRoot(
    viewModel: ResultViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    // 어두운 배경이라 상태바 아이콘을 밝게
    SystemBarIcons(darkIcons = false)

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        AdHelper.showInterstitial(context)
    }

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
                is ResultEvent.OpenUrl -> {
                    context.openExternalUrl(event.url)
                }
            }
        }
    }

    ResultScreen(
        state = state,
        onAction = viewModel::onAction
    )
}
