package com.devhjs.runningtracker.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
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
         position = CameraPosition.fromLatLngZoom(LatLng(37.5665, 126.9780), 15f)
    }

    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, 15f)
            )
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
