package com.devhjs.runningtracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDAO {

    /** @return 저장된 기록의 id */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: RunEntity): Long

    @Delete
    suspend fun deleteRun(run: RunEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPath(path: RunPathEntity)

    @Query("DELETE FROM run_path WHERE runId = :runId")
    suspend fun deletePathByRunId(runId: Int)

    /** 기록과 경로를 한 트랜잭션으로 저장한다. 경로가 비어있으면 경로 행은 만들지 않는다. */
    @Transaction
    suspend fun insertRunWithPath(run: RunEntity, encodedPath: String) {
        val runId = insertRun(run).toInt()
        if (encodedPath.isNotEmpty()) {
            insertPath(RunPathEntity(runId = runId, encodedPath = encodedPath))
        }
    }

    /** 기록과 경로를 한 트랜잭션으로 지운다. */
    @Transaction
    suspend fun deleteRunWithPath(run: RunEntity) {
        run.id?.let { deletePathByRunId(it) }
        deleteRun(run)
    }

    @Query("SELECT * FROM running_table WHERE id = :id")
    suspend fun getRunById(id: Int): RunEntity?

    @Query("SELECT encodedPath FROM run_path WHERE runId = :runId")
    suspend fun getEncodedPath(runId: Int): String?

    @Query("SELECT * FROM running_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): Flow<List<RunEntity>>

    @Query("SELECT * FROM running_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMillis(): Flow<List<RunEntity>>

    @Query("SELECT * FROM running_table ORDER BY caloriesBurned DESC")
    fun getAllRunsSortedByCaloriesBurned(): Flow<List<RunEntity>>

    @Query("SELECT * FROM running_table ORDER BY avgSpeedInKMH DESC")
    fun getAllRunsSortedByAvgSpeed(): Flow<List<RunEntity>>

    @Query("SELECT * FROM running_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): Flow<List<RunEntity>>

    @Query("SELECT SUM(timeInMillis) FROM running_table")
    fun getTotalTimeInMillis(): Flow<Long>

    @Query("SELECT SUM(caloriesBurned) FROM running_table")
    fun getTotalCaloriesBurned(): Flow<Int>

    @Query("SELECT SUM(distanceInMeters) FROM running_table")
    fun getTotalDistance(): Flow<Int>

    @Query("SELECT AVG(avgSpeedInKMH) FROM running_table")
    fun getTotalAvgSpeed(): Flow<Float>
}
