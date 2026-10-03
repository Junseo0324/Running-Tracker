package com.devhjs.runningtracker.presentation.detail

import androidx.compose.runtime.Stable
import com.devhjs.runningtracker.domain.affiliate.CoupangRecommendation
import com.devhjs.runningtracker.domain.model.Run
import com.google.android.gms.maps.model.LatLng

@Stable
data class RunDetailState(
    val run: Run? = null,
    /** 저장된 이동 경로. 경로 저장 기능 이전의 기록이면 비어있고, 대신 run.img 를 보여준다. */
    val pathPoints: List<List<LatLng>> = emptyList(),
    val isLoaded: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val coupangRecommendation: CoupangRecommendation? = null
)
