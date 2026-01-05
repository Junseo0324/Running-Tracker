package com.devhjs.runningtracker.presentation.home

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val permissionState = rememberMultiplePermissionsState(permissions = permissionsToRequest)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(37.5665, 126.9780), 15f)
    }

    LaunchedEffect(true) {
        viewModel.event.collect { event ->
            when(event) {
                is HomeEvent.Navigate -> onNavigate(event.route)
            }
        }
    }

    LaunchedEffect(permissionState.allPermissionsGranted) {
        viewModel.onAction(HomeAction.OnPermissionsResult(permissionState.allPermissionsGranted))
    }

    LaunchedEffect(state.currentLocation) {
        state.currentLocation?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, 15f)
            )
        }
    }

    HomeScreen(
        state = state,
        onAction = { action ->
            when(action) {
                HomeAction.OnStartClick -> {
                    if (permissionState.allPermissionsGranted) {
                        viewModel.onAction(action)
                    } else {
                        permissionState.launchMultiplePermissionRequest()
                    }
                }
                else -> viewModel.onAction(action)
            }
        },
        cameraPositionState = cameraPositionState
    )
}
