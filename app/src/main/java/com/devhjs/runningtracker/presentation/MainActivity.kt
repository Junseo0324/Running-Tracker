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
import com.devhjs.runningtracker.presentation.navigation.Navigation
import com.devhjs.runningtracker.ui.theme.RunningTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var gpsStatusMonitor: com.devhjs.runningtracker.data.connectivity.GpsStatusMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        gpsStatusMonitor.startMonitoring()
        
        enableEdgeToEdge()
        setContent {
            RunningTrackerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Navigation()
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gpsStatusMonitor.stopMonitoring()
    }
}



