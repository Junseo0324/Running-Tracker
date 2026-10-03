package com.devhjs.runningtracker.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.devhjs.runningtracker.core.Constants.MAP_ZOOM
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun FullScreenMap(
    modifier: Modifier = Modifier,
    isMyLocationEnabled: Boolean = false,
    isMyLocationButtonEnabled: Boolean = false,
    currentLocation: LatLng? = null
) {
    val cameraPositionState = rememberCameraPositionState {
         position = CameraPosition.fromLatLngZoom(LatLng(37.5665, 126.9780), MAP_ZOOM)
    }
    // 화면에 들어와 첫 위치를 받았는지. 첫 위치에서만 줌을 기본값으로 맞춘다.
    var hasCenteredOnUser by remember { mutableStateOf(false) }

    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            val update = if (hasCenteredOnUser) {
                // 이후에는 중심만 따라가서 사용자가 바꾼 줌을 유지한다.
                CameraUpdateFactory.newLatLng(it)
            } else {
                // 지도 준비 타이밍에 따라 초기 줌이 적용되지 않고 멀리 축소된 채 남는 경우가 있어
                // 첫 위치에서는 줌까지 함께 맞춘다.
                CameraUpdateFactory.newLatLngZoom(it, MAP_ZOOM)
            }
            hasCenteredOnUser = true
            cameraPositionState.animate(update)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = isMyLocationEnabled,
                isBuildingEnabled = true,
                isTrafficEnabled = false
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                compassEnabled = false,
                myLocationButtonEnabled = isMyLocationButtonEnabled
            )
        )
    }
}
