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
    
    // 목록 로딩이 끝난 뒤에 전면 광고를 띄운다.
    // 진입과 동시에 띄우면 전체 화면 크리에이티브 할당이 목록 조회의 메모리 피크와 겹친다.
    LaunchedEffect(state.isLoaded) {
        if (state.isLoaded) {
            AdHelper.showInterstitialForHistory(context, frequency = 3)
        }
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
