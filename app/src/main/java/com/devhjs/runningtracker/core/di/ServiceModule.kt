package com.devhjs.runningtracker.core.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.devhjs.runningtracker.R
import com.devhjs.runningtracker.core.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.devhjs.runningtracker.core.Constants.NOTIFICATION_CHANNEL_ID
import com.devhjs.runningtracker.presentation.MainActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {



    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app: Context
    ) = NotificationCompat.Builder(app, NOTIFICATION_CHANNEL_ID)
        .setAutoCancel(false)
        .setOngoing(true)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Running Tracker")
        .setContentText("00:00:00")
}
