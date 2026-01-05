package com.devhjs.runningtracker.presentation.run

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.devhjs.runningtracker.core.Constants.ACTION_PAUSE_SERVICE
import com.devhjs.runningtracker.core.Constants.ACTION_START_OR_RESUME_SERVICE
import com.devhjs.runningtracker.core.Constants.ACTION_STOP_SERVICE
import com.devhjs.runningtracker.core.Constants.MAP_ZOOM
import com.devhjs.runningtracker.core.Constants.POLYLINE_COLOR
import com.devhjs.runningtracker.core.Constants.POLYLINE_WIDTH
import com.devhjs.runningtracker.core.util.TrackingUtility
import com.devhjs.runningtracker.presentation.components.StatsCardItem
import com.devhjs.runningtracker.presentation.navigation.Screen
import com.devhjs.runningtracker.presentation.viewmodels.MainViewModel
import com.devhjs.runningtracker.service.TrackingService
import com.devhjs.runningtracker.ui.theme.RunningBlack
import com.devhjs.runningtracker.ui.theme.RunningGreen
import com.devhjs.runningtracker.ui.theme.TextWhite
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import java.lang.Math.round

@Composable
fun RunScreen(
    navController: NavController,
    runId: String = "-1", // unused for new run
    viewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isTracking by TrackingService.isTracking.observeAsState(false)
    val pathPoints by TrackingService.pathPoints.observeAsState(listOf())
    val curTimeInMillis by TrackingService.timeRunInMillis.observeAsState(0L)

    val cameraPositionState = rememberCameraPositionState()
    val fusedLocationClient = remember { com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context) }

    // Initial Map Center
    LaunchedEffect(Unit) {
        try {
            val locationResult = fusedLocationClient.lastLocation
            locationResult.addOnSuccessListener { location ->
                location?.let {
                    cameraPositionState.position = CameraPosition.fromLatLngZoom(
                        LatLng(it.latitude, it.longitude),
                        MAP_ZOOM
                    )
                }
            }
        } catch (e: SecurityException) {
            // Permission not granted or error
        }
    }

    // Additional state for "Locked" mode (as seen in screenshot)
    var isLocked by remember { mutableStateOf(false) }

    // Keep screen on
    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Observe latest point to move camera
    LaunchedEffect(key1 = pathPoints) {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    // Calculations
    val distanceInMeters = if (pathPoints.isNotEmpty()) {
        pathPoints.fold(0f) { acc, polyline ->
            acc + TrackingUtility.calculatePolylineLength(polyline)
        }
    } else 0f
    
    val avgSpeed = round((distanceInMeters / 1000f) / (curTimeInMillis / 1000f / 3600f) * 10) / 10f
    val caloriesBurned = ((distanceInMeters / 1000f) * 60).toInt() // Dummy formula

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Map Background
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            pathPoints.forEach { polyline ->
                Polyline(
                    points = polyline,
                    color = Color(POLYLINE_COLOR),
                    width = POLYLINE_WIDTH
                )
            }
        }

        // 2. Overlay Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TOP: Timer Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = RunningBlack.copy(alpha = 0.85f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "운동 시간",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = TrackingUtility.getFormattedStopWatchTime(curTimeInMillis),
                        color = TextWhite,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatsCardItem(label = "km", value = String.format("%.2f", distanceInMeters / 1000f))
                        StatsCardItem(label = "평균 페이스", value = if (avgSpeed > 0) String.format("%.1f", avgSpeed) else "0.0") 
                        StatsCardItem(label = "kcal", value = "$caloriesBurned")
                    }
                }
            }

            // MIDDLE: Nothing (Map visible)

            // BOTTOM: Controls
            // If Running: Show Lock & Big Pause Button
            // If Paused: Show Resume & Stop Buttons
            
            Box(
                 modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                 contentAlignment = Alignment.Center
            ) {
                if (isTracking && !isLocked) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                         // Lock Button
                        IconButton(
                            onClick = { isLocked = true },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = TextWhite)
                        }

                        // PAUSE Button (Big Green)
                        Button(
                            onClick = {
                                sendCommandToService(ACTION_PAUSE_SERVICE, context)
                            },
                             modifier = Modifier.size(80.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RunningGreen),
                             shape = RoundedCornerShape(24.dp)
                        ) {
                             Icon(
                                 imageVector = Icons.Default.Pause, 
                                 contentDescription = "Pause",
                                 tint = RunningBlack,
                                 modifier = Modifier.size(32.dp)
                             )
                        }
                        
                         // Dummy spacer to balance layout if needed, or another button
                         Spacer(modifier = Modifier.size(56.dp)) 
                    }
                } else if (isLocked) {
                     // Unlock Button (Long press logic in real app, click for now)
                     Button(
                        onClick = { isLocked = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RunningBlack.copy(alpha=0.9f))
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = RunningGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("길게 눌러 잠금 해제 (클릭)", color = TextWhite)
                    }
                } else {
                    // PAUSED State -> Resume / Stop
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                         // Resume
                        Button(
                            onClick = {
                                sendCommandToService(ACTION_START_OR_RESUME_SERVICE, context)
                            },
                             modifier = Modifier
                                 .weight(1f)
                                 .height(56.dp)
                                 .padding(end = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RunningGreen),
                             shape = RoundedCornerShape(16.dp)
                        ) {
                             Row(verticalAlignment = Alignment.CenterVertically) {
                                 Icon(Icons.Default.PlayArrow, contentDescription = null, tint = RunningBlack)
                                 Spacer(modifier = Modifier.width(8.dp))
                                 Text("재개", color = RunningBlack, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                             }
                        }

                        // Stop (Finish)
                        Button(
                            onClick = {
                                sendCommandToService(ACTION_STOP_SERVICE, context)
                                navController.navigate(Screen.ResultScreen.route)
                            },
                             modifier = Modifier
                                 .weight(1f)
                                 .height(56.dp)
                                 .padding(start = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)), // Red for stop
                             shape = RoundedCornerShape(16.dp)
                        ) {
                             Row(verticalAlignment = Alignment.CenterVertically) {
                                 Icon(Icons.Default.Stop, contentDescription = null, tint = TextWhite)
                                 Spacer(modifier = Modifier.width(8.dp))
                                 Text("종료", color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                             }
                        }
                    }
                }
            }
        }
        
        // Paused Overlay (Dimmed background)
        if (!isTracking && curTimeInMillis > 0L) {
             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Text(
                    text = "일시정지됨",
                    color = TextWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

fun sendCommandToService(action: String, context: android.content.Context) {
    android.content.Intent(context, TrackingService::class.java).also {
        it.action = action
        context.startService(it)
    }
}
