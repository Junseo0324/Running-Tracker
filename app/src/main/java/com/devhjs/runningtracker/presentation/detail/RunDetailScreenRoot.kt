package com.devhjs.runningtracker.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devhjs.runningtracker.presentation.util.SystemBarIcons
import com.devhjs.runningtracker.presentation.util.openExternalUrl

@Composable
fun RunDetailScreenRoot(
    viewModel: RunDetailViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    // 어두운 배경이라 상태바 아이콘을 밝게
    SystemBarIcons(darkIcons = false)

    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                RunDetailEvent.NavigateUp -> onNavigateUp()
                is RunDetailEvent.OpenUrl -> context.openExternalUrl(event.url)
            }
        }
    }

    RunDetailScreen(
        state = state,
        onAction = viewModel::onAction
    )
}
