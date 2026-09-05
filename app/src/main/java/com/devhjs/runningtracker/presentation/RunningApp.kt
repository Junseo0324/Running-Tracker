package com.devhjs.runningtracker.presentation

import android.app.Application
import com.devhjs.runningtracker.BuildConfig
import com.devhjs.runningtracker.presentation.util.AdHelper
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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

        // MobileAds 초기화는 디스크 I/O 를 동반하므로 메인 스레드에서 하지 않는다.
        // Activity 가 아닌 application context 로 초기화해 Activity 참조를 남기지 않는다.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            MobileAds.initialize(this@RunningApp) {
                AdHelper.loadInterstitial(this@RunningApp)
            }
        }
    }
}
