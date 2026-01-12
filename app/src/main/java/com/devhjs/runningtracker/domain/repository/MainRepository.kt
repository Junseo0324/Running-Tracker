package com.devhjs.runningtracker.domain.repository

import com.devhjs.runningtracker.domain.model.Run
import kotlinx.coroutines.flow.Flow

interface MainRepository {
    suspend fun insertRun(run: Run)
    suspend fun deleteRun(run: Run)

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
