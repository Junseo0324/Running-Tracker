package com.devhjs.runningtracker.presentation.history

import androidx.compose.runtime.Stable
import com.devhjs.runningtracker.domain.model.Run

@Stable
data class RunHistoryState(
    val runs: List<Run> = emptyList(),
    /** 저장소에서 첫 조회가 끝났는지. 전면 광고를 목록 로딩 이후로 미루는 데 쓴다. */
    val isLoaded: Boolean = false
)
