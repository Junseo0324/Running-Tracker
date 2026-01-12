package com.devhjs.runningtracker.domain.manager

import android.location.Location
import com.devhjs.runningtracker.service.Polylines
import kotlinx.coroutines.flow.StateFlow

/**
 * 실시간 러닝 세션의 상태를 관리하는 매니저 인터페이스입니다.
 *
 * 이 매니저는 "현재 진행 중인 러닝"의 데이터(시간, 경로, 상태)를 인메모리에서 관리합니다.
 */
interface RunningManager {

    // --- State ---
    
    /** 현재 러닝이 진행 중(기록 중)인지 여부 */
    val isTracking: StateFlow<Boolean>

    /** 현재 러닝의 경로 데이터 */
    val pathPoints: StateFlow<Polylines>

    /** 현재 러닝 시간 (밀리초) */
    val durationInMillis: StateFlow<Long>

    /** GPS 활성화 여부 */
    val isGpsEnabled: StateFlow<Boolean>

    // --- Actions ---

    /** 러닝을 시작하거나 재개합니다. (isTracking = true) */
    suspend fun startResumeRun()

    /** 러닝을 일시정지합니다. (isTracking = false) */
    suspend fun pauseRun()

    /** 러닝을 종료하고 데이터를 초기화합니다. */
    suspend fun stopRun()

    /** 새로운 위치를 경로에 추가합니다. */
    suspend fun addLocation(location: Location?)

    /** 
     * 경로에 빈 구간을 추가합니다. 
     * 일시정지 후 다시 시작할 때, 이전 경로와 이어지지 않도록 끊어주는 역할을 합니다. 
     */
    suspend fun addEmptyPolyline()

    /** 현재 러닝 시간을 업데이트합니다. */
    suspend fun updateDuration(timeInMillis: Long)

    // --- Persistence ---

    /** 현재 상태를 임시 파일에 저장합니다. (앱 종료 대비) */
    suspend fun persistState()

    /** 임시 파일에서 상태를 복구합니다. (앱 재시작 시) */
    suspend fun restoreState()
}
