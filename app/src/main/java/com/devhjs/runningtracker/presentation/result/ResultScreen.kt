package com.devhjs.runningtracker.presentation.result


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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.devhjs.runningtracker.core.Constants.MAP_ZOOM
import com.devhjs.runningtracker.core.Constants.POLYLINE_COLOR
import com.devhjs.runningtracker.core.Constants.POLYLINE_WIDTH
import com.devhjs.runningtracker.core.util.TrackingUtility
import com.devhjs.runningtracker.presentation.components.PrimaryButton
import com.devhjs.runningtracker.presentation.components.StatsCardItem

import com.devhjs.runningtracker.ui.theme.RunningBlack
import com.devhjs.runningtracker.ui.theme.RunningGreen
import com.devhjs.runningtracker.ui.theme.TextGrey
import com.devhjs.runningtracker.ui.theme.TextWhite
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun ResultScreen(
    state: ResultState= ResultState(),
    onAction: (ResultAction) -> Unit = {}
) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(key1 = state.pathPoints) {
        if (state.pathPoints.isNotEmpty() && state.pathPoints.last().isNotEmpty()) {
            cameraPositionState.move(
                 CameraUpdateFactory.newLatLngZoom(
                     state.pathPoints.last().last(),
                     MAP_ZOOM
                 )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Map Background
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                scrollGesturesEnabled = true,
                zoomGesturesEnabled = true
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

        // 2. Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { 
                     onAction(ResultAction.OnDiscardClick)
                },
                modifier = Modifier.background(RunningBlack.copy(alpha=0.5f), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
            }
            
            Text(
                text = "Workout Summary", 
                color = TextWhite, 
                fontWeight = FontWeight.Bold, 
                fontSize = 20.sp,
                modifier = Modifier
                    .background(RunningBlack.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )

            IconButton(
                onClick = { /* Share logic */ },
                 modifier = Modifier.background(RunningBlack.copy(alpha=0.5f), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = TextWhite)
            }
        }

        // 3. Bottom Summary Card
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp),
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
                    StatsCardItem(label = "Time", value = TrackingUtility.getFormattedStopWatchTime(state.timeInMillis))
                    StatsCardItem(label = "Avg Pace", value = "${String.format("%.2f", state.avgSpeed)}'")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatsCardItem(label = "Calories", value = "${state.caloriesBurned}")
                    StatsCardItem(label = "Elevation", value = "0 m") // Dummy
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
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultScreenPreview() {
    ResultScreen()
}
