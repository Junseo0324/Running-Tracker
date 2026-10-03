package com.devhjs.runningtracker.data.repository

import com.devhjs.runningtracker.core.util.PolylineEncoder
import com.devhjs.runningtracker.data.local.RunDAO
import com.devhjs.runningtracker.data.mapper.toDomain
import com.devhjs.runningtracker.data.mapper.toEntity
import com.devhjs.runningtracker.domain.model.Run
import com.devhjs.runningtracker.domain.repository.MainRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MainRepositoryImpl @Inject constructor(
    val runDao: RunDAO
) : MainRepository {

    override suspend fun insertRun(run: Run, pathPoints: List<List<LatLng>>) {
        val encodedPath = withContext(Dispatchers.Default) { PolylineEncoder.encodePath(pathPoints) }
        runDao.insertRunWithPath(run.toEntity(), encodedPath)
    }

    override suspend fun deleteRun(run: Run) {
        runDao.deleteRunWithPath(run.toEntity())
    }

    override suspend fun getRunById(id: Int): Run? = runDao.getRunById(id)?.toDomain()

    override suspend fun getRunPath(runId: Int): List<List<LatLng>> {
        val encoded = runDao.getEncodedPath(runId) ?: return emptyList()
        return withContext(Dispatchers.Default) { PolylineEncoder.decodePath(encoded) }
    }

    override fun getAllRunsSortedByDate(): Flow<List<Run>> {
        return runDao.getAllRunsSortedByDate().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAllRunsSortedByDistance(): Flow<List<Run>> {
        return runDao.getAllRunsSortedByDistance().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAllRunsSortedByTimeInMillis(): Flow<List<Run>> {
         return runDao.getAllRunsSortedByTimeInMillis().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAllRunsSortedByAvgSpeed(): Flow<List<Run>> {
         return runDao.getAllRunsSortedByAvgSpeed().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getAllRunsSortedByCaloriesBurned(): Flow<List<Run>> {
         return runDao.getAllRunsSortedByCaloriesBurned().map { entities -> entities.map { it.toDomain() } }
    }

    override fun getTotalAvgSpeed(): Flow<Float> = runDao.getTotalAvgSpeed()

    override fun getTotalDistance(): Flow<Int> = runDao.getTotalDistance()

    override fun getTotalCaloriesBurned(): Flow<Int> = runDao.getTotalCaloriesBurned()

    override fun getTotalTimeInMillis(): Flow<Long> = runDao.getTotalTimeInMillis()
}
