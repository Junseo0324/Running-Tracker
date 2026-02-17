package com.devhjs.runningtracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.devhjs.runningtracker.core.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.devhjs.runningtracker.presentation.designsystem.RunningTrackerTheme
import com.devhjs.runningtracker.presentation.navigation.Navigation
import com.devhjs.runningtracker.presentation.util.AdHelper
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 광고 초기화 처리
        MobileAds.initialize(this)
        AdHelper.loadInterstitial(this)
        
        // 알림에서 앱을 열었는지 확인
        val shouldNavigateToRun = intent?.action == ACTION_SHOW_TRACKING_FRAGMENT
        
        enableEdgeToEdge()
        setContent {
            RunningTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Navigation(shouldNavigateToRun = shouldNavigateToRun)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
