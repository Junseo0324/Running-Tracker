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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devhjs.runningtracker.core.Constants.POLYLINE_COLOR
import com.devhjs.runningtracker.core.Constants.POLYLINE_WIDTH
import com.devhjs.runningtracker.core.util.TimeUtils
import com.devhjs.runningtracker.presentation.components.StatsCardItem
import com.devhjs.runningtracker.presentation.designsystem.RunningBlack
import com.devhjs.runningtracker.presentation.designsystem.RunningGreen
import com.devhjs.runningtracker.presentation.designsystem.TextWhite
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable

fun RunScreen(
    state: RunState= RunState(),
    onAction: (RunAction) -> Unit= {},
    cameraPositionState: CameraPositionState = rememberCameraPositionState()
) {
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = true),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false
            )
        ) {
            state.pathPoints.forEach { polyline ->
                Polyline(
                    points = polyline,
                    color = Color(POLYLINE_COLOR),
                    width = POLYLINE_WIDTH
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        text = TimeUtils.getFormattedStopWatchTime(state.curTimeInMillis),
                        color = TextWhite,
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatsCardItem(label = "km", value = String.format("%.2f", state.distanceInMeters / 1000f), icon= Icons.Default.Speed)
                        StatsCardItem(label = "평균 페이스", value = if (state.avgSpeed > 0) String.format("%.1f", state.avgSpeed) else "0.0", icon= Icons.Default.Speed)
                        StatsCardItem(label = "kcal", value = "${state.caloriesBurned}", icon =Icons.Default.LocalFireDepartment)
                    }
                }
            }

            Box(
                 modifier = Modifier
                     .fillMaxWidth()
                     .padding(bottom = 32.dp),
                 contentAlignment = Alignment.Center
            ) {
                if (state.isTracking && !state.isLocked) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = { onAction(RunAction.OnToggleLock) },
                            modifier = Modifier
                                .size(56.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = "Lock", tint = TextWhite)
                        }

                        Button(
                            onClick = { onAction(RunAction.OnPause) },
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
                        
                         Spacer(modifier = Modifier.size(56.dp)) 
                    }
                } else if (state.isLocked) {
                     Button(
                        onClick = { onAction(RunAction.OnToggleLock) },
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { onAction(RunAction.OnResume) },
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
                                 Text("시작", color = RunningBlack, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                             }
                        }

                        Button(
                            onClick = { onAction(RunAction.OnFinish) },
                             modifier = Modifier
                                 .weight(1f)
                                 .height(56.dp)
                                 .padding(start = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252)), 
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
        
        if (!state.isTracking && state.curTimeInMillis > 0L) {
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

        if (!state.isGpsEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.9f))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "GPS가 꺼져있습니다. 위치 추적을 위해 GPS를 켜주세요.",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun RunScreenPreview() {
    RunScreen()
}


@Preview(showBackground = true)
@Composable
private fun RunScreenPreviewGpsEnabled() {
    RunScreen(
        state = RunState(isGpsEnabled = false)
    )
}


@Preview(showBackground = true)
@Composable
private fun RunScreenPreviewIsPaused() {
    RunScreen(
        state = RunState(
            isTracking = false,
            curTimeInMillis = 1000L
        )
    )
}


@Preview(showBackground = true)
@Composable
private fun RunScreenPreviewIsLocked() {
    RunScreen(
        state = RunState(isLocked = true)
    )
}