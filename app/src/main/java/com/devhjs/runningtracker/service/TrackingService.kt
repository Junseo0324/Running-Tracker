package com.devhjs.runningtracker.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.devhjs.runningtracker.R
import com.devhjs.runningtracker.core.Constants.ACTION_PAUSE_SERVICE
import com.devhjs.runningtracker.core.Constants.ACTION_START_OR_RESUME_SERVICE
import com.devhjs.runningtracker.core.Constants.ACTION_STOP_SERVICE
import com.devhjs.runningtracker.core.Constants.FASTEST_LOCATION_INTERVAL
import com.devhjs.runningtracker.core.Constants.LOCATION_UPDATE_INTERVAL
import com.devhjs.runningtracker.core.Constants.NOTIFICATION_CHANNEL_ID
import com.devhjs.runningtracker.core.Constants.NOTIFICATION_CHANNEL_NAME
import com.devhjs.runningtracker.core.Constants.NOTIFICATION_ID
import com.devhjs.runningtracker.core.Constants.TIMER_UPDATE_INTERVAL
import com.devhjs.runningtracker.core.util.TrackingUtility
import com.devhjs.runningtracker.domain.repository.TrackingRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder

    @Inject
    lateinit var trackingRepository: TrackingRepository

    lateinit var curNotificationBuilder: NotificationCompat.Builder

    private val gpsBroadcastReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == android.location.LocationManager.PROVIDERS_CHANGED_ACTION) {
                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
                val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
                lifecycleScope.launch {
                    trackingRepository.setGpsEnabled(isGpsEnabled)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder
        
        // Register GPS Status Receiver
        registerReceiver(gpsBroadcastReceiver, android.content.IntentFilter(android.location.LocationManager.PROVIDERS_CHANGED_ACTION))
        
        // Initial GPS Check
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
        lifecycleScope.launch {
            trackingRepository.setGpsEnabled(isGpsEnabled)
        }
        
        // Initialize Repository (postInitialValues logic moved to repo implicitly or explicitly here)
        lifecycleScope.launch {
            // Try to restore state first
            trackingRepository.restoreState()
            
            // If we restored data, sync local variables
            val restoredTime = trackingRepository.timeRunInMillis.value
            if (restoredTime > 0L) {
                timeRun = restoredTime
                isFirstRun = false
                // Note: We don't automatically resume the timer here. User has to press start/resume.
                // Or if we want START_STICKY to auto-resume, we'd need to know if we were running.
                // For now, let's just restore data so it's not lost.
            } else {
                 trackingRepository.clearData()
            }
        }
        
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        lifecycleScope.launch {
            trackingRepository.isTracking.collect { isTracking ->
                updateLocationTracking(isTracking)
                updateNotificationTrackingState(isTracking)
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(gpsBroadcastReceiver)
    }

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
        return START_STICKY
    }

    private var isTimerEnabled = false
    private var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimestamp = 0L

    private fun startTimer() {
        lifecycleScope.launch {
            trackingRepository.addEmptyPolyline()
            trackingRepository.setIsTracking(true)
        }
        
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = true
        
        CoroutineScope(Dispatchers.Main).launch {
            while (isTimerEnabled) { // Check local flag to stop coroutine loop
                // time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted
                // update the new lapTime
                val timeRunInMillis = timeRun + lapTime
                trackingRepository.updateTimeRunInMillis(timeRunInMillis)
                
                if (timeRunInMillis >= lastSecondTimestamp + 1000L) {
                    lastSecondTimestamp += 1000L
                    updateNotificationTime(timeRunInMillis)
                    
                    // Persist state every 5 seconds (approx) for crash recovery
                    if (lastSecondTimestamp % 5000L == 0L) {
                        lifecycleScope.launch {
                            trackingRepository.persistState()
                        }
                    }
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun updateNotificationTime(timeInMillis: Long) {
        if (!serviceKilled) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val notification = curNotificationBuilder
                .setContentText(TrackingUtility.getFormattedStopWatchTime(timeInMillis))
            notificationManager.notify(NOTIFICATION_ID, notification.build())
        }
    }

    private fun pauseService() {
        lifecycleScope.launch {
            trackingRepository.setIsTracking(false)
        }
        isTimerEnabled = false
    }

    private fun killService() {
        serviceKilled = true
        isFirstRun = true
        pauseService()
        lifecycleScope.launch {
            trackingRepository.clearData()
        }
        stopForeground(true)
        stopSelf()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
                    .setMinUpdateIntervalMillis(FASTEST_LOCATION_INTERVAL)
                    .build()
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            // Need to check isTracking from repo? 
            // We can check local var or repo. 
            // Since updateLocationTracking is reactive to flow, we can assume if this callback fires, we are likely tracking.
            // But good to double check.
            if(isTimerEnabled) { // Use local flag for sync safety or repo.isTracking.value
                 result.locations.let { locations ->
                    for (location in locations) {
                        lifecycleScope.launch {
                            trackingRepository.addPathPoint(location)
                        }
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun startForegroundService() {
        startTimer()
        // isTracking set in startTimer

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())
    }

    private fun updateNotificationTrackingState(isTracking: Boolean) {
        val notificationActionText = if (isTracking) "Pause" else "Resume"
        val pendingIntent = if (isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }

        if (!serviceKilled) {
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_launcher_foreground, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
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
