package com.devhjs.runningtracker.debug

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.core.graphics.createBitmap
import com.devhjs.runningtracker.data.local.RunDAO
import com.devhjs.runningtracker.data.local.RunEntity
import com.devhjs.runningtracker.data.local.RunningDatabase
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.random.Random

/**
 * OOM 재현용 디버그 전용 시더.
 *
 * 저장된 러닝 기록이 많은 사용자를 흉내내기 위해, 실제 앱이 만드는 것과 동일한 규격
 * (800x800 PNG) 의 경로 이미지를 가진 더미 기록을 대량으로 삽입한다.
 *
 * 사용법 (-n 으로 컴포넌트를 반드시 명시할 것.
 *  암시적 브로드캐스트는 매니페스트 리시버에 전달되지 않는다):
 *
 *   adb shell am broadcast -n com.devhjs.runningtracker/.debug.SeedRunsReceiver \
 *     -a com.devhjs.runningtracker.debug.SEED_RUNS --ei count 50
 *
 *   adb shell am broadcast -n com.devhjs.runningtracker/.debug.SeedRunsReceiver \
 *     -a com.devhjs.runningtracker.debug.CLEAR_RUNS
 *
 * debug 소스셋에만 존재하므로 릴리스 빌드에는 포함되지 않는다.
 */
class SeedRunsReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SeedEntryPoint {
        fun runDao(): RunDAO
        fun database(): RunningDatabase
    }

    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors
            .fromApplication(context.applicationContext, SeedEntryPoint::class.java)
        val dao = entryPoint.runDao()
        val database = entryPoint.database()

        val pendingResult = goAsync()
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        scope.launch {
            try {
                when (intent.action) {
                    ACTION_SEED -> {
                        val count = intent.getIntExtra(EXTRA_COUNT, 50)
                        seed(dao, count)
                    }
                    ACTION_CLEAR -> clear(database)
                    else -> Timber.w("알 수 없는 액션: ${intent.action}")
                }
            } catch (e: Exception) {
                Timber.e(e, "시더 실행 실패")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private suspend fun seed(dao: RunDAO, count: Int) {
        Timber.d("더미 러닝 기록 ${count}개 삽입 시작")
        val now = System.currentTimeMillis()

        // 시더 자체가 OOM 되지 않도록 한 건씩 만들고 즉시 해제한다.
        repeat(count) { i ->
            val bitmap = generateDummyPathBitmap()
            val entity = RunEntity(
                timestamp = now - i * ONE_DAY_MILLIS,
                avgSpeedInKMH = 8f + Random.nextFloat() * 6f,
                distanceInMeters = 3_000 + Random.nextInt(12_000),
                timeInMillis = (20L + Random.nextInt(70)) * 60L * 1000L,
                caloriesBurned = 200 + Random.nextInt(600),
                img = bitmap
            )
            dao.insertRun(entity)
            bitmap.recycle()

            if ((i + 1) % 10 == 0) Timber.d("  ${i + 1}/$count 삽입 완료")
        }
        Timber.d("더미 러닝 기록 ${count}개 삽입 완료")
    }

    /**
     * DAO 에 디버그 전용 메서드를 추가하지 않기 위해 raw SQL 로 지운다.
     * 전체 조회 후 삭제하는 방식은 그 자체로 모든 비트맵을 디코딩하므로(= 재현하려는 바로 그 버그)
     * 여기서는 쓸 수 없다. Room 의 무효화 트리거는 raw DELETE 에도 동작하므로 UI 는 정상 갱신된다.
     */
    private fun clear(database: RunningDatabase) {
        database.openHelper.writableDatabase.execSQL("DELETE FROM running_table")
        Timber.d("모든 러닝 기록 삭제 완료")
    }

    /**
     * ResultViewModel.generatePolylineBitmap() 과 동일한 규격(800x800, 검정 배경 + 초록 경로)으로
     * 더미 이미지를 만든다. PNG 압축 후 크기가 실제와 비슷해야 재현이 의미가 있다.
     */
    private fun generateDummyPathBitmap(): Bitmap {
        val size = 800
        val bitmap = createBitmap(size, size)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.BLACK)

        val paint = Paint().apply {
            color = Color.GREEN
            strokeWidth = 10f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        // 랜덤 워크로 실제 러닝 경로와 비슷한 모양을 그린다.
        val path = Path()
        var x = size / 2f
        var y = size / 2f
        path.moveTo(x, y)
        repeat(300) {
            x = (x + Random.nextInt(-40, 41)).coerceIn(50f, size - 50f)
            y = (y + Random.nextInt(-40, 41)).coerceIn(50f, size - 50f)
            path.lineTo(x, y)
        }
        canvas.drawPath(path, paint)
        return bitmap
    }

    companion object {
        const val ACTION_SEED = "com.devhjs.runningtracker.debug.SEED_RUNS"
        const val ACTION_CLEAR = "com.devhjs.runningtracker.debug.CLEAR_RUNS"
        const val EXTRA_COUNT = "count"
        private const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}
