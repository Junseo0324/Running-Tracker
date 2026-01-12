package com.devhjs.runningtracker.data.local

import kotlinx.serialization.Serializable

/**
 * 파일 저장을 위한 데이터 클래스입니다.
 * 앱이 종료될 때 현재 러닝 상태를 임시 저장하는 데 사용됩니다.
 */
@Serializable
data class TempRunState(
    val timeInMillis: Long,
    val pathPoints: List<List<SerializableLatLng>>
)
