package com.devhjs.runningtracker.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.devhjs.runningtracker.R
import com.devhjs.runningtracker.core.Constants
import com.devhjs.runningtracker.core.Constants.ACTION_PAUSE_SERVICE
import com.devhjs.runningtracker.core.Constants.ACTION_START_OR_RESUME_SERVICE
import com.devhjs.runningtracker.core.Constants.ACTION_STOP_SERVICE
import com.devhjs.runningtracker.core.Constants.NOTIFICATION_CHANNEL_ID
import com.devhjs.runningtracker.core.Constants.NOTIFICATION_CHANNEL_NAME
import com.devhjs.runningtracker.core.Constants.NOTIFICATION_ID
import com.devhjs.runningtracker.core.Constants.TIMER_UPDATE_INTERVAL
import com.devhjs.runningtracker.core.util.LocationUtils
import com.devhjs.runningtracker.core.util.TimeUtils
import com.devhjs.runningtracker.domain.location.LocationClient
import com.devhjs.runningtracker.domain.manager.RunningManager
import com.devhjs.runningtracker.presentation.MainActivity
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

/**
 * 러닝 기록을 백그라운드에서 추적하기 위한 포그라운드 서비스입니다.
 * 앱이 화면에 보이지 않을 때도 위치를 수집하고, 타이머를 갱신하며, 알림창을 통해 상태를 표시합니다.
 */
@AndroidEntryPoint
class TrackingService : LifecycleService() {

    // 첫 실행 여부 확인 플래그
    var isFirstRun = true
    // 서비스가 강제 종료(kill) 되었는지 확인하는 플래그
    var serviceKilled = false

    @Inject
    lateinit var locationClient: LocationClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var runningManager: RunningManager

    private val mainActivityPendingIntent: PendingIntent by lazy {
        val intent = Intent(this, MainActivity::class.java).apply {
            action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        }
        PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // 현재 상태에 따라 업데이트될 알림 빌더
    lateinit var trackingNotificationBuilder: NotificationCompat.Builder


    override fun onCreate() {
        super.onCreate()
        trackingNotificationBuilder = baseNotificationBuilder
        
        // 서비스 생성 시 초기화 로직
        lifecycleScope.launch {
            // 이전에 저장된 상태가 있다면 복원 시도
            runningManager.restoreState()
            
            // 복원된 데이터가 있다면 로컬 변수 동기화
            val restoredTime = runningManager.durationInMillis.value
            if (restoredTime > 0L) {
                timeRun = restoredTime
                isFirstRun = false
                // 참고: 사용자가 직접 시작/재개를 누르지 않는 한 타이머를 자동으로 재개하지 않음.
                // 데이터 유실 방지를 위해 복원만 수행.
            } else {
                 // 복원할 데이터가 없으면 러닝 상태 초기화
                 runningManager.stopRun()
            }
        }
        
        // 러닝 상태(tracking 여부)를 관찰하여 위치 추적 및 알림 업데이트 관리
        lifecycleScope.launch {
            runningManager.isTracking.collect { isTracking ->
                updateLocationTracking(isTracking)
                updateNotificationTrackingState()
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }

    // 서비스 시작 명령(Intent Action) 처리
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }
            }
        }
        super.onStartCommand(intent, flags, startId)
        return START_STICKY // 시스템에 의해 강제 종료 시 서비스 재생성 시도
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    // 타이머 시작 및 러닝 시작 로직
    private fun startTimer() {
        lifecycleScope.launch {
            // 새로운 경로(Polyline) 리스트 추가 및 러닝 상태 시작
            runningManager.addEmptyPolyline()
            runningManager.startResumeRun()
        }
        
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        
        // 메인 스레드에서 타이머 코루틴 실행
        CoroutineScope(Dispatchers.Main).launch {
            while (isTimerEnabled) {
                // 현재 시간과 시작 시간의 차이 계산 (랩 타임)
                lapTime = System.currentTimeMillis() - timeStarted
                // 전체 러닝 시간 갱신
                val timeRunInMillis = timeRun + lapTime
                runningManager.updateDuration(timeRunInMillis)
                
                // 1초마다 알림창 시간 업데이트 및 상태 저장
                if (timeRunInMillis >= lastSecondTimestamp + 1000L) {
                    lastSecondTimestamp += 1000L
                    updateNotificationTime(timeRunInMillis)
                    
                    // 약 5초마다 데이터 영구 저장 (앱 충돌 대비)
                    if (lastSecondTimestamp % 5000L == 0L) {
                        lifecycleScope.launch {
                            runningManager.persistState()
                        }
                    }
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            // 타이머 루프 종료 시 누적 시간 저장
            timeRun += lapTime
        }
    }

    // 알림창에 표시되는 러닝 시간 업데이트
    private fun updateNotificationTime(timeInMillis: Long) {
        if (!serviceKilled) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = trackingNotificationBuilder
                .setContentText(TimeUtils.getFormattedStopWatchTime(timeInMillis))
            notificationManager.notify(NOTIFICATION_ID, notification.build())
        }
    }

    // 서비스 일시 정지 (타이머 중지)
    private fun pauseService() {
        lifecycleScope.launch {
            runningManager.pauseRun()
        }
        isTimerEnabled = false
    }

    // 서비스 완전 종료
    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        lifecycleScope.launch {
            runningManager.stopRun()
        }
        stopForeground(true)
        stopSelf()
    }

    private var locationJob: Job? = null

    // 위치 추적 업데이트 로직 (권한 필요)
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (LocationUtils.hasLocationPermissions(this)) {
                // 기존 작업 취소 후 새로운 위치 수집 시작
                locationJob?.cancel()
                locationJob = lifecycleScope.launch {
                    locationClient.getLocationFlow()
                        .collect { location ->
                            // 타이머가 작동 중일 때만 위치 데이터 저장
                            if (isTimerEnabled) {
                                runningManager.addLocation(location)
                                Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                            }
                        }
                }
            }
        } else {
            // 추적 중지 시 위치 수집 작업 취소
            locationJob?.cancel()
            locationJob = null
        }
    }

    // 포그라운드 서비스 시작 및 알림 채널 생성
    private fun startForegroundService() {
        startTimer()
        // startTimer 내부에서 isTracking 상태가 변경됨

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
    }

    // 러닝 상태에 따라 알림창 업데이트 (시간만 표시)
    private fun updateNotificationTrackingState() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val currentTimeInMillis = runningManager.durationInMillis.value
        val formattedTime = TimeUtils.getFormattedStopWatchTime(currentTimeInMillis)

        trackingNotificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.workout_run)
            .setContentTitle("Running Tracker")
            .setContentText(formattedTime)
            .setContentIntent(mainActivityPendingIntent)

        if (!serviceKilled) {
            notificationManager.notify(NOTIFICATION_ID, trackingNotificationBuilder.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}
