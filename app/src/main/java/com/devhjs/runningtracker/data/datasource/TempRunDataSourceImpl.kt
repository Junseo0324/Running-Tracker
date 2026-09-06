package com.devhjs.runningtracker.data.datasource

import android.content.Context
import com.devhjs.runningtracker.data.local.SerializableLatLng
import com.devhjs.runningtracker.data.local.TempRunState
import com.devhjs.runningtracker.domain.datasource.TempRunDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TempRunDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TempRunDataSource {

    /**
     * 러닝 중 5초마다 호출된다. JSON 직렬화와 파일 쓰기를 호출자의 스레드에서 하면
     * 메인 스레드를 붙잡게 되므로(TrackingService 는 lifecycleScope = Main 에서 호출한다)
     * 반드시 IO 디스패처로 넘긴다.
     */
    override suspend fun persistState(
        timeInMillis: Long,
        pathPoints: List<List<SerializableLatLng>>
    ) = withContext(Dispatchers.IO) {
        try {
            val state = TempRunState(
                timeInMillis = timeInMillis,
                pathPoints = pathPoints
            )
            val jsonString = json.encodeToString(state)
            context.openFileOutput(TEMP_RUN_FILE, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: Exception) {
            Timber.e(e, "임시 러닝 상태 저장 실패")
        }
    }

    override suspend fun restoreState(): TempRunState? = withContext(Dispatchers.IO) {
        try {
            val file = File(context.filesDir, TEMP_RUN_FILE)
            if (file.exists()) {
                val jsonString =
                    context.openFileInput(TEMP_RUN_FILE).bufferedReader().use { it.readText() }
                json.decodeFromString<TempRunState>(jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            Timber.e(e, "임시 러닝 상태 복구 실패")
            null
        }
    }

    override suspend fun clearData() = withContext(Dispatchers.IO) {
        try {
            context.deleteFile(TEMP_RUN_FILE)
            Unit
        } catch (e: Exception) {
            Timber.e(e, "임시 러닝 상태 삭제 실패")
        }
    }

    companion object {
        private const val TEMP_RUN_FILE = "temp_run_state.json"

        // 호출마다 새로 만들 필요가 없다.
        private val json = Json
    }
}
