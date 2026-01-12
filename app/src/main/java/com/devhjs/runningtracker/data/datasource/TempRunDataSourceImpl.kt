package com.devhjs.runningtracker.data.datasource

import android.content.Context
import com.devhjs.runningtracker.data.local.SerializableLatLng
import com.devhjs.runningtracker.data.local.TempRunState
import com.devhjs.runningtracker.domain.datasource.TempRunDataSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TempRunDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TempRunDataSource {

    override suspend fun persistState(timeInMillis: Long, pathPoints: List<List<SerializableLatLng>>) {
        try {
            val state = TempRunState(
                timeInMillis = timeInMillis,
                pathPoints = pathPoints
            )
            val jsonString = Json.encodeToString(state)
            context.openFileOutput(TEMP_RUN_FILE, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun restoreState(): TempRunState? {
        return try {
            val file = File(context.filesDir, TEMP_RUN_FILE)
            if (file.exists()) {
                val jsonString = context.openFileInput(TEMP_RUN_FILE).bufferedReader().use { it.readText() }
                Json.decodeFromString<TempRunState>(jsonString)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override suspend fun clearData() {
        try {
            context.deleteFile(TEMP_RUN_FILE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val TEMP_RUN_FILE = "temp_run_state.json"
    }
}
