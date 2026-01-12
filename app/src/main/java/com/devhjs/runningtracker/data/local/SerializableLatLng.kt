package com.devhjs.runningtracker.data.local

import kotlinx.serialization.Serializable

/**
 * Google Maps의 LatLng는 직렬화를 지원하지 않아 만든 대체 클래스입니다.
 * Room이나 파일 저장 시 사용됩니다.
 */
@Serializable
data class SerializableLatLng(
    val lat: Double,
    val lng: Double
)
