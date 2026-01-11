package com.devhjs.runningtracker.core.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng

object MapUtils {

    /**
     * 지도상의 경로(Polyline) 좌표들의 전체 거리를 계산합니다.
     * @param polyline 위도/경도(LatLng) 좌표들의 리스트
     * @return 총 거리 (미터 단위)
     */
    fun calculatePolylineLength(polyline: List<LatLng>): Float {
        var distance = 0f
        for (i in 0..polyline.size - 2) {
            val pos1 = polyline[i]
            val pos2 = polyline[i + 1]
            val result = FloatArray(1)
            Location.distanceBetween(
                pos1.latitude,
                pos1.longitude,
                pos2.latitude,
                pos2.longitude,
                result
            )
            distance += result[0]
        }
        return distance
    }
}
