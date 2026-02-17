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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devhjs.runningtracker.core.Constants.POLYLINE_COLOR
import com.devhjs.runningtracker.core.Constants.POLYLINE_WIDTH
import com.devhjs.runningtracker.core.util.TimeUtils
import com.devhjs.runningtracker.presentation.components.AdMobBanner
import com.devhjs.runningtracker.presentation.components.PrimaryButton
import com.devhjs.runningtracker.presentation.components.StatsCardItem
import com.devhjs.runningtracker.presentation.designsystem.RunningBlack
import com.devhjs.runningtracker.presentation.designsystem.RunningGreen
import com.devhjs.runningtracker.presentation.designsystem.TextGrey
import com.devhjs.runningtracker.presentation.designsystem.TextWhite
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLngBounds
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
    // 지도 위치 제어
    val cameraPositionState = rememberCameraPositionState()
    val localDensity = LocalDensity.current
    // 하단 결과 높이 저장 변수
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

             state.pathPoints.forEach { polyline ->
                Polyline(
                    points = polyline,
                    color = Color(POLYLINE_COLOR),
                    width = POLYLINE_WIDTH
                )
            }
        }

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
                    Text(text = "총 거리", color = TextGrey, fontSize = 14.sp)
                    Text(
                        text = "${String.format("%.2f", state.distanceInMeters / 1000f)} km",
                        color = RunningGreen,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatsCardItem(label = "평균 페이스", value = "${String.format("%.2f", state.avgSpeed)}'", icon= Icons.Default.Speed)
                        StatsCardItem(label = "칼로리", value = "${state.caloriesBurned}", icon =Icons.Default.LocalFireDepartment)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatsCardItem(label = "시간", value = TimeUtils.getFormattedStopWatchTime(state.timeInMillis), icon = Icons.Default.AccessTime)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    PrimaryButton(
                        text = "기록 저장",
                        onClick = {
                            onAction(ResultAction.OnSaveClick)
                        }
                    )
                }
            }

            AdMobBanner(
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
