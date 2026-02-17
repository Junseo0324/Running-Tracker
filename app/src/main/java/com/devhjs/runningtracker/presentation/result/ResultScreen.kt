package com.devhjs.runningtracker.presentation.result


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.devhjs.runningtracker.core.Constants.POLYLINE_COLOR
import com.devhjs.runningtracker.core.Constants.POLYLINE_WIDTH
import com.devhjs.runningtracker.core.util.TimeUtils
import com.devhjs.runningtracker.presentation.components.PrimaryButton
import com.devhjs.runningtracker.presentation.components.StatsCardItem
import com.devhjs.runningtracker.presentation.designsystem.RunningBlack
import com.devhjs.runningtracker.presentation.designsystem.RunningGreen
import com.devhjs.runningtracker.presentation.designsystem.TextGrey
import com.devhjs.runningtracker.presentation.designsystem.TextWhite
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun ResultScreen(
    state: ResultState= ResultState(),
    onAction: (ResultAction) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    LaunchedEffect(Unit) {
        com.devhjs.runningtracker.presentation.util.AdHelper.showInterstitial(context)
    }

    val cameraPositionState = rememberCameraPositionState()

    val localDensity = androidx.compose.ui.platform.LocalDensity.current
    var googleMap: GoogleMap? by remember { mutableStateOf(null) }
    var bottomCardHeight by remember { mutableStateOf(0.dp) }

    LaunchedEffect(key1 = state.pathPoints, key2 = bottomCardHeight) {
        if (state.pathPoints.isNotEmpty() && state.pathPoints.flatten().isNotEmpty() && bottomCardHeight > 0.dp) {
            val boundsBuilder = LatLngBounds.Builder()
            state.pathPoints.flatten().forEach { boundsBuilder.include(it) }
            val bounds = boundsBuilder.build()

            val paddingPx = with(localDensity) { 64.dp.toPx() }.toInt()

            cameraPositionState.animate(
                 CameraUpdateFactory.newLatLngBounds(bounds, paddingPx)
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Map Background
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            contentPadding = PaddingValues(top = 64.dp, bottom = bottomCardHeight + 50.dp, start = 16.dp, end = 16.dp),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                scrollGesturesEnabled = true,
                zoomGesturesEnabled = true
            )
        ) {
            MapEffect(Unit) { map ->
                googleMap = map
            }
             state.pathPoints.forEach { polyline ->
                Polyline(
                    points = polyline,
                    color = Color(POLYLINE_COLOR),
                    width = POLYLINE_WIDTH
                )
            }
        }

        // 2. Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp)
        ) {
            IconButton(
                onClick = {
                     onAction(ResultAction.OnDiscardClick)
                },
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(RunningBlack.copy(alpha=0.5f), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
            }

            Text(
                text = "운동 결과",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(RunningBlack.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        // 3. Bottom Summary Card & Ad
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    bottomCardHeight = with(localDensity) { coordinates.size.height.toDp() }
                }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = RunningBlack.copy(alpha = 0.95f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "TOTAL DISTANCE", color = TextGrey, fontSize = 14.sp)
                    Text(
                        text = "${String.format("%.2f", state.distanceInMeters / 1000f)} km",
                        color = RunningGreen,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatsCardItem(label = "Time", value = TimeUtils.getFormattedStopWatchTime(state.timeInMillis), icon = Icons.Default.AccessTime)
                        StatsCardItem(label = "Avg Pace", value = "${String.format("%.2f", state.avgSpeed)}'", icon= Icons.Default.Speed)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatsCardItem(label = "Calories", value = "${state.caloriesBurned}", icon =Icons.Default.LocalFireDepartment)
                        StatsCardItem(label = "Elevation", value = "0 m", icon = Icons.Default.Cable) // Dummy
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    PrimaryButton(
                        text = "Save Workout",
                        onClick = {
                            onAction(ResultAction.OnSaveClick)
                        }
                    )
                }
            }

            com.devhjs.runningtracker.presentation.components.AdMobBanner(
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultScreenPreview() {
    ResultScreen()
}
