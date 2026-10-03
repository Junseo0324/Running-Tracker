package com.devhjs.runningtracker.core.util

import java.util.concurrent.TimeUnit

object TimeUtils {

    /**
     * 밀리초 단위의 시간을 스톱워치 형식의 문자열(HH:MM:SS 또는 HH:MM:SS:mm)로 변환
     * @param ms 변환할 시간 (밀리초)
     * @param includeMillis 밀리초 단위 포함 여부 (기본값: false)
     * @return 포맷팅된 시간 문자열
     */
    fun getFormattedStopWatchTime(ms: Long, includeMillis: Boolean = false): String {
        var milliseconds = ms
        val hours = TimeUnit.MILLISECONDS.toHours(milliseconds)
        milliseconds -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds)
        milliseconds -= TimeUnit.MINUTES.toMillis(minutes)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds)
        if (!includeMillis) {
            return "${if(hours < 10) "0" else ""}$hours:" +
                    "${if(minutes < 10) "0" else ""}$minutes:" +
                    "${if(seconds < 10) "0" else ""}$seconds"
        }
        milliseconds -= TimeUnit.SECONDS.toMillis(seconds)
        milliseconds /= 10
        return "${if(hours < 10) "0" else ""}$hours:" +
                "${if(minutes < 10) "0" else ""}$minutes:" +
                "${if(seconds < 10) "0" else ""}$seconds:" +
                "${if(milliseconds < 10) "0" else ""}$milliseconds"
    }

    /**
     * 평균 속도(km/h)를 러닝 페이스 문자열(1km 당 분'초")로 변환
     * 예: 10km/h -> 6'00"
     * 속도가 0 이거나 페이스가 너무 느려 의미가 없으면 -'--" 를 돌려준다.
     */
    fun getFormattedPace(speedInKmh: Float): String {
        if (speedInKmh <= 0f || speedInKmh.isNaN() || speedInKmh.isInfinite()) return EMPTY_PACE
        val secondsPerKm = Math.round(3600f / speedInKmh)
        if (secondsPerKm >= MAX_PACE_SECONDS) return EMPTY_PACE
        val minutes = secondsPerKm / 60
        val seconds = secondsPerKm % 60
        return "$minutes'${if (seconds < 10) "0" else ""}$seconds\""
    }

    private const val EMPTY_PACE = "-'--\""

    /** 1km 에 100분 이상 걸리는 페이스는 걷기보다 느려 표시하지 않는다. */
    private const val MAX_PACE_SECONDS = 100 * 60
}
