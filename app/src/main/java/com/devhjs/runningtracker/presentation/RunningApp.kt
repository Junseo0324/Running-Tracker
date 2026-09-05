package com.devhjs.runningtracker.presentation

import android.app.Application
import com.devhjs.runningtracker.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class RunningApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Timber 가 심어지지 않으면 앱 전역의 Timber.d/e 호출이 모두 무시된다.
        // 릴리스 빌드에서는 로그를 남기지 않는다.
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
