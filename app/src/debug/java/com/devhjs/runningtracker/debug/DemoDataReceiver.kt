package com.devhjs.runningtracker.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.location.Location
import androidx.core.graphics.createBitmap
import com.devhjs.runningtracker.core.util.ImageUtils
import com.devhjs.runningtracker.core.util.MapUtils
import com.devhjs.runningtracker.core.util.PolylineEncoder
import com.devhjs.runningtracker.data.local.RunDAO
import com.devhjs.runningtracker.data.local.RunEntity
import com.devhjs.runningtracker.data.local.RunningDatabase
import com.devhjs.runningtracker.domain.manager.RunningManager
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.Calendar
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * 스토어 스크린샷용 디버그 전용 데모 데이터.
 *
 * 나라별 대표 러닝 코스(assets/demo/{region}.json)로 기록 목록과 진행 중 러닝 상태를 채운다.
 *
 *   # 기록 목록 채우기 (기존 기록은 지운다)
 *   adb shell am broadcast -n com.devhjs.runningtracker/.debug.DemoDataReceiver \
 *     -a com.devhjs.runningtracker.debug.DEMO_SEED --es region kr
 *
 *   # 진행 중 러닝 상태 만들기 (fraction: 코스 진행률, tracking: 러닝 중 여부)
 *   adb shell am broadcast -n com.devhjs.runningtracker/.debug.DemoDataReceiver \
 *     -a com.devhjs.runningtracker.debug.DEMO_RUN --es region kr --ef fraction 0.65 --ez tracking true
 *
 * 러닝 상태는 TrackingService 없이 RunningManager 에 직접 넣으므로 시간이 흐르지 않고 화면이 고정된다.
 */
class DemoDataReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DemoEntryPoint {
        fun runDao(): RunDAO
        fun database(): RunningDatabase
        fun runningManager(): RunningManager
    }

    @Serializable
    private data class Course(val loop: Boolean, val points: List<List<Double>>)

    @Serializable
    private data class Region(val name: String, val home: List<Double>, val main: Course, val alt: Course)

    /** 기록 화면에 채울 러닝. 거리는 코스 길이에 맞춰 바퀴 수(또는 왕복)로 맞춘다. */
    private data class Plan(
        val daysAgo: Int,
        val hour: Int,
        val minute: Int,
        val useMain: Boolean,
        val targetKm: Double,
        val paceSecPerKm: Int
    )

    private val history = listOf(
        Plan(daysAgo = 1, hour = 6, minute = 42, useMain = true, targetKm = 5.0, paceSecPerKm = 342),
        Plan(daysAgo = 2, hour = 19, minute = 15, useMain = false, targetKm = 3.0, paceSecPerKm = 365),
        Plan(daysAgo = 4, hour = 7, minute = 5, useMain = true, targetKm = 2.6, paceSecPerKm = 330),
        Plan(daysAgo = 5, hour = 20, minute = 10, useMain = false, targetKm = 6.0, paceSecPerKm = 372),
        Plan(daysAgo = 7, hour = 6, minute = 55, useMain = true, targetKm = 7.5, paceSecPerKm = 350),
        Plan(daysAgo = 9, hour = 7, minute = 20, useMain = true, targetKm = 5.0, paceSecPerKm = 338)
    )

    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, DemoEntryPoint::class.java)
        val regionCode = intent.getStringExtra(EXTRA_REGION) ?: "kr"
        val pendingResult = goAsync()

        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val region = loadRegion(context, regionCode)
                when (intent.action) {
                    ACTION_SEED -> seed(entryPoint, region)
                    ACTION_RUN -> setRunState(
                        manager = entryPoint.runningManager(),
                        region = region,
                        fraction = intent.getFloatExtra(EXTRA_FRACTION, 1f),
                        tracking = intent.getBooleanExtra(EXTRA_TRACKING, false)
                    )
                    else -> Timber.w("알 수 없는 액션: ${intent.action}")
                }
            } catch (e: Exception) {
                Timber.e(e, "데모 데이터 생성 실패")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun loadRegion(context: Context, code: String): Region {
        val text = context.assets.open("demo/$code.json").bufferedReader().use { it.readText() }
        return json.decodeFromString<Region>(text)
    }

    private suspend fun seed(entryPoint: DemoEntryPoint, region: Region) {
        val db = entryPoint.database().openHelper.writableDatabase
        db.execSQL("DELETE FROM run_path")
        db.execSQL("DELETE FROM running_table")

        val dao = entryPoint.runDao()
        history.forEach { plan ->
            val course = if (plan.useMain) region.main else region.alt
            val path = buildPath(course, plan.targetKm)
            val distance = MapUtils.calculatePolylineLength(path)
            val timeInMillis = (distance / 1000f * plan.paceSecPerKm).roundToLong() * 1000L

            val bitmap = generatePathBitmap(path)
            val img = ImageUtils.bitmapToBytes(bitmap).also { bitmap.recycle() }

            val entity = RunEntity(
                timestamp = timestampOf(plan.daysAgo, plan.hour, plan.minute),
                avgSpeedInKMH = (distance / 1000f) / (timeInMillis / 3_600_000f),
                distanceInMeters = distance.roundToInt(),
                timeInMillis = timeInMillis,
                caloriesBurned = ((distance / 1000f) * 60).toInt(),
                img = img
            )
            dao.insertRunWithPath(entity, PolylineEncoder.encodePath(listOf(path)))
        }
        Timber.d("${region.name} 데모 기록 ${history.size}개 생성")
    }

    private suspend fun setRunState(manager: RunningManager, region: Region, fraction: Float, tracking: Boolean) {
        val full = buildPath(region.main, RUN_TARGET_KM)
        val count = (full.size * fraction.coerceIn(0f, 1f)).roundToInt().coerceAtLeast(2)
        val path = full.take(count)
        val distance = MapUtils.calculatePolylineLength(path)

        manager.stopRun()
        path.forEach { point ->
            manager.addLocation(Location("demo").apply {
                latitude = point.latitude
                longitude = point.longitude
            })
        }
        manager.updateDuration((distance / 1000f * RUN_PACE_SEC_PER_KM).roundToLong() * 1000L)
        if (tracking) manager.startResumeRun() else manager.pauseRun()

        val last = path.last()
        Timber.d("데모 러닝 상태: ${region.name} ${distance.roundToInt()}m, 현재 위치 ${last.latitude},${last.longitude}")
    }

    /** 순환 코스는 바퀴 수로, 편도 코스는 목표가 길면 왕복으로 거리를 맞춘다. */
    private fun buildPath(course: Course, targetKm: Double): List<LatLng> {
        val one = course.points.map { LatLng(it[0], it[1]) }
        val oneKm = MapUtils.calculatePolylineLength(one) / 1000.0
        return if (course.loop) {
            val laps = (targetKm / oneKm).roundToInt().coerceAtLeast(1)
            one + (2..laps).flatMap { one.drop(1) }
        } else if (targetKm > oneKm * 1.5) {
            one + one.reversed().drop(1)
        } else {
            one
        }
    }

    private fun timestampOf(daysAgo: Int, hour: Int, minute: Int): Long =
        Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -daysAgo)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    /** ResultViewModel.generatePolylineBitmap() 과 같은 규격(400x400, 검정 배경 + 초록 경로). */
    private fun generatePathBitmap(path: List<LatLng>): Bitmap {
        val size = 400
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.BLACK)

        val paint = Paint().apply {
            color = Color.GREEN
            strokeWidth = 10f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        val minLat = path.minOf { it.latitude }
        val maxLat = path.maxOf { it.latitude }
        val minLng = path.minOf { it.longitude }
        val maxLng = path.maxOf { it.longitude }
        val latDiff = maxLat - minLat
        val lngDiff = maxLng - minLng
        val padding = 50f
        val scale = minOf((size - padding * 2) / lngDiff, (size - padding * 2) / latDiff)
        val offsetX = (size - lngDiff * scale) / 2
        val offsetY = (size - latDiff * scale) / 2

        val linePath = Path()
        path.forEachIndexed { i, p ->
            val x = ((p.longitude - minLng) * scale + offsetX).toFloat()
            val y = (size - ((p.latitude - minLat) * scale + offsetY)).toFloat()
            if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
        }
        canvas.drawPath(linePath, paint)
        return bitmap
    }

    companion object {
        private const val ACTION_SEED = "com.devhjs.runningtracker.debug.DEMO_SEED"
        private const val ACTION_RUN = "com.devhjs.runningtracker.debug.DEMO_RUN"
        private const val EXTRA_REGION = "region"
        private const val EXTRA_FRACTION = "fraction"
        private const val EXTRA_TRACKING = "tracking"

        /** 진행 중 러닝과 결과 화면에 쓸 목표 거리와 페이스. */
        private const val RUN_TARGET_KM = 5.0
        private const val RUN_PACE_SEC_PER_KM = 335

        private val json = Json { ignoreUnknownKeys = true }
    }
}
