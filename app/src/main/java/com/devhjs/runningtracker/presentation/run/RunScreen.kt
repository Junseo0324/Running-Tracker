package com.devhjs.runningtracker.presentation.run


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devhjs.runningtracker.R
import com.devhjs.runningtracker.core.Constants.POLYLINE_COLOR
import com.devhjs.runningtracker.core.Constants.POLYLINE_WIDTH
import com.devhjs.runningtracker.core.util.LocationUtils
import com.devhjs.runningtracker.core.util.TimeUtils
import com.devhjs.runningtracker.presentation.components.StatsCardItem
import com.devhjs.runningtracker.presentation.components.StatusBarScrim
import com.devhjs.runningtracker.presentation.designsystem.RunningBlack
import com.devhjs.runningtracker.presentation.designsystem.RunningGreen
import com.devhjs.runningtracker.presentation.designsystem.TextWhite
import com.devhjs.runningtracker.presentation.util.formatDistanceKm
import com.devhjs.runningtracker.presentation.util.ltrIsolate
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
    val context = LocalContext.current
    val hasLocationPermission = LocationUtils.hasLocationPermissions(context)

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            contentPadding = WindowInsets.systemBars.asPaddingValues(),
            properties = MapProperties(isMyLocationEnabled = hasLocationPermission),
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

        StatusBarScrim(modifier = Modifier.align(Alignment.TopCenter))

        if (!state.isTracking && state.curTimeInMillis > 0L) {
             Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Text(
                    text = stringResource(R.string.paused),
                    color = TextWhite,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
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
                        text = stringResource(R.string.elapsed_time),
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
                        StatsCardItem(label = "km", value = formatDistanceKm(state.distanceInMeters, fractionDigits = 2), icon= Icons.Default.Speed)
                        StatsCardItem(label = stringResource(R.string.avg_pace), value = ltrIsolate(TimeUtils.getFormattedPace(state.avgSpeed)), icon= Icons.Default.Speed)
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
                            Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.cd_lock), tint = TextWhite)
                        }

                        Button(
                            onClick = { onAction(RunAction.OnPause) },
                             modifier = Modifier.size(80.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RunningGreen),
                             shape = RoundedCornerShape(24.dp)
                        ) {
                             Icon(
                                 imageVector = Icons.Default.Pause, 
                                 contentDescription = stringResource(R.string.cd_pause),
                                 tint = RunningBlack,
                                 modifier = Modifier.size(32.dp)
                             )
                        }
                        
                         Spacer(modifier = Modifier.size(56.dp)) 
                    }
                } else if (state.isLocked) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .background(RunningBlack.copy(alpha=0.9f), RoundedCornerShape(100.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onLongPress = { onAction(RunAction.OnToggleLock) }
                                )
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, tint = RunningGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.hold_to_unlock), color = TextWhite, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = {
                                if (LocationUtils.hasLocationPermissions(context)) {
                                    onAction(RunAction.OnResume)
                                } else {
                                    Toast.makeText(
                                        context,
                                        R.string.location_permission_required,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
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
                                 Text(
                                     text = stringResource(if (state.curTimeInMillis > 0L) R.string.resume else R.string.start), 
                                     color = RunningBlack, 
                                     fontSize = 18.sp, 
                                     fontWeight = FontWeight.Bold
                                 )
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
                                 Text(stringResource(R.string.finish), color = TextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                             }
                        }
                    }
                }
            }
        }

        if (!state.isGpsEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Red.copy(alpha = 0.9f))
                    .statusBarsPadding()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.gps_off_banner),
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