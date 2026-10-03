package com.devhjs.runningtracker.domain.repository

import com.devhjs.runningtracker.domain.model.Run
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    /** 기록과 이동 경로(일시정지로 끊긴 구간 목록)를 함께 저장한다. */
    suspend fun insertRun(run: Run, pathPoints: List<List<LatLng>> = emptyList())

    /** 기록과 이동 경로를 함께 지운다. */
    suspend fun deleteRun(run: Run)

    suspend fun getRunById(id: Int): Run?

    /** 저장된 경로. 경로 저장 기능 이전에 만들어진 기록이면 빈 리스트다. */
    suspend fun getRunPath(runId: Int): List<List<LatLng>>

    fun getAllRunsSortedByDate(): Flow<List<Run>>
    fun getAllRunsSortedByDistance(): Flow<List<Run>>
    fun getAllRunsSortedByTimeInMillis(): Flow<List<Run>>
    fun getAllRunsSortedByAvgSpeed(): Flow<List<Run>>
    fun getAllRunsSortedByCaloriesBurned(): Flow<List<Run>>
    
    fun getTotalAvgSpeed(): Flow<Float>
    fun getTotalDistance(): Flow<Int>
    fun getTotalCaloriesBurned(): Flow<Int>
    fun getTotalTimeInMillis(): Flow<Long>
}
