package com.devhjs.runningtracker.service

import android.content.Intent
import androidx.lifecycle.LifecycleService

class TrackingService : LifecycleService() {
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }
}
