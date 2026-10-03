package com.devhjs.runningtracker.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.devhjs.runningtracker.core.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.devhjs.runningtracker.presentation.designsystem.RunningBlack
import com.devhjs.runningtracker.presentation.designsystem.RunningTrackerTheme
import com.devhjs.runningtracker.presentation.navigation.Navigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 알림에서 앱을 열었는지 확인
        val shouldNavigateToRun = intent?.action == ACTION_SHOW_TRACKING_FRAGMENT
        
        enableEdgeToEdge()
        setContent {
            RunningTrackerTheme {
                // 화면이 시스템 바 뒤까지 그려지도록 여기서는 인셋을 적용하지 않는다.
                // 각 화면이 지도/배경은 끝까지 채우고 버튼 · 텍스트만 시스템 바를 피해 배치한다.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(RunningBlack)
                ) {
                    Navigation(shouldNavigateToRun = shouldNavigateToRun)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
