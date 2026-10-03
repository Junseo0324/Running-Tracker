package com.devhjs.runningtracker.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devhjs.runningtracker.core.Constants.MAP_ZOOM
import com.devhjs.runningtracker.core.Constants.POLYLINE_COLOR
import com.devhjs.runningtracker.core.Constants.POLYLINE_WIDTH
import com.devhjs.runningtracker.core.util.ImageUtils
import com.devhjs.runningtracker.core.util.TimeUtils
import com.devhjs.runningtracker.presentation.designsystem.RunningGreen
import com.devhjs.runningtracker.presentation.designsystem.TextGrey
import com.devhjs.runningtracker.presentation.designsystem.TextWhite
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

/**
 * 거리(크게) + 평균 페이스 · 시간 · 칼로리 한 줄.
 * 결과 화면과 기록 상세 화면이 함께 쓴다.
 */
@Composable
fun RunStatsSummary(
    distanceInMeters: Float,
    avgSpeedInKmh: Float,
    timeInMillis: Long,
    caloriesBurned: Int,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = String.format("%.2f", distanceInMeters / 1000f),
            color = RunningGreen,
            fontSize = 72.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic
        )
        Text(text = "킬로미터", color = TextGrey, fontSize = 16.sp)

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            SummaryStat(
                value = TimeUtils.getFormattedPace(avgSpeedInKmh),
                label = "평균 페이스",
                modifier = Modifier.weight(1f)
            )
            SummaryStat(
                value = TimeUtils.getFormattedStopWatchTime(timeInMillis),
                label = "시간",
                modifier = Modifier.weight(1f)
            )
            SummaryStat(
                value = "$caloriesBurned",
                label = "칼로리",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = value,
            color = TextWhite,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(text = label, color = TextGrey, fontSize = 13.sp)
    }
}

/**
 * 이동 경로가 카드에 꽉 차도록 맞춘 보기 전용 지도.
 * 스크롤 화면 안에 들어가므로 지도 제스처는 모두 끈다. (켜두면 스크롤과 충돌한다)
 */
@Composable
fun RouteMap(
    pathPoints: List<List<LatLng>>,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()
    val localDensity = LocalDensity.current
    // 지도 크기가 정해지기 전에 newLatLngBounds 를 쓰면 예외가 나므로 로드 이후에 카메라를 옮긴다.
    var isMapLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(pathPoints, isMapLoaded) {
        val allPoints = pathPoints.flatten()
        if (!isMapLoaded || allPoints.isEmpty()) return@LaunchedEffect

        val bounds = LatLngBounds.Builder().apply { allPoints.forEach { include(it) } }.build()
        val update = if (bounds.northeast == bounds.southwest) {
            // 점이 하나뿐이면 bounds 로 맞출 수 없어 기본 줌으로 보여준다.
            CameraUpdateFactory.newLatLngZoom(bounds.center, MAP_ZOOM)
        } else {
            CameraUpdateFactory.newLatLngBounds(bounds, with(localDensity) { 32.dp.roundToPx() })
        }
        cameraPositionState.move(update)
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(
                compassEnabled = false,
                mapToolbarEnabled = false,
                myLocationButtonEnabled = false,
                rotationGesturesEnabled = false,
                scrollGesturesEnabled = false,
                tiltGesturesEnabled = false,
                zoomControlsEnabled = false,
                zoomGesturesEnabled = false
            ),
            onMapLoaded = { isMapLoaded = true }
        ) {
            pathPoints.forEach { polyline ->
                Polyline(
                    points = polyline,
                    color = Color(POLYLINE_COLOR),
                    width = POLYLINE_WIDTH
                )
            }
        }
    }
}

/**
 * 경로 좌표가 없는 예전 기록용. 저장 당시 그려둔 경로 이미지를 지도 카드 자리에 보여준다.
 * 카드 크기에 맞춰 축소 디코딩한다.
 */
@Composable
fun RouteImage(
    imageBytes: ByteArray?,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.background(Color.Black)) {
        val sizePx = with(LocalDensity.current) { maxWidth.roundToPx() }
        val bitmap = remember(imageBytes, sizePx) {
            imageBytes?.let { ImageUtils.decodeSampledBitmap(it, sizePx, sizePx)?.asImageBitmap() }
        }
        bitmap?.let {
            Image(
                bitmap = it,
                contentDescription = "Run Path",
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
