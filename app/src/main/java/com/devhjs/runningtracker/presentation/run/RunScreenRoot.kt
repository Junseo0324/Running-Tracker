package com.devhjs.runningtracker.presentation.run

import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devhjs.runningtracker.service.TrackingService
import com.google.android.gms.maps.model.LatLng

@Composable
fun RunScreenRoot(
    viewModel: RunViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var initialLocation by remember { mutableStateOf<LatLng?>(null) }
    
    val cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState()

    LaunchedEffect(true) {
        viewModel.event.collect { event ->
            when(event) {
                is RunEvent.Navigate -> onNavigate(event.route)
                is RunEvent.ServiceAction -> {
                    android.content.Intent(context, TrackingService::class.java).also {
                        it.action = event.action
                        context.startService(it)
                    }
                }
            }
        }
    }

    val fusedLocationClient = remember { com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context) }
    LaunchedEffect(Unit) {
        if(state.pathPoints.isEmpty()) {
            try {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnSuccessListener { location: Location? ->
                    location?.let {
                        initialLocation = LatLng(it.latitude, it.longitude)
                    }
                }
            } catch (e: SecurityException) {
            }
        }
    }
    
    // Initial Map Center
    LaunchedEffect(initialLocation) {
        initialLocation?.let {
            if(state.pathPoints.isEmpty()) {
                cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(
                    it,
                    com.devhjs.runningtracker.core.Constants.MAP_ZOOM
                )
            }
        }
    }

    // Camera Follow Logic
    LaunchedEffect(key1 = state.currentLocation) {
        state.currentLocation?.let {
            cameraPositionState.animate(
                com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(it, com.devhjs.runningtracker.core.Constants.MAP_ZOOM)
            )
        }
    }

    // Keep screen on
    DisposableEffect(Unit) {
        val window = (context as? android.app.Activity)?.window
        window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    RunScreen(
        state = state,
        onAction = viewModel::onAction,
        cameraPositionState = cameraPositionState
    )
}
