package com.devhjs.runningtracker.presentation.run

import android.content.Intent
import android.location.Location
import android.view.WindowManager
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
import com.devhjs.runningtracker.core.Constants
import com.devhjs.runningtracker.service.TrackingService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
fun RunScreenRoot(
    viewModel: RunViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 위치 정보 저장
    var initialLocation by remember { mutableStateOf<LatLng?>(null) }

    // Google Map의 카메라 상태 제어
    val cameraPositionState = rememberCameraPositionState()

    // 사용자의 현재 위치를 가져옴
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }


    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is RunEvent.Navigate -> onNavigate(event.route)
                is RunEvent.ServiceAction -> {
                    Intent(context, TrackingService::class.java).also {
                        it.action = event.action
                        context.startService(it)
                    }
                }
            }
        }
    }

    // 초기 위치 설정
    LaunchedEffect(Unit) {
        // path가 없을 때 fusedLocationClient 를 사용해 위치 지정
        if (state.pathPoints.isEmpty()) {
            try {
                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnSuccessListener { location: Location? ->
                    location?.let {
                        initialLocation = LatLng(it.latitude, it.longitude)
                    }
                }
            } catch (_: SecurityException) {
            }
        }
    }

    // 지도의 중심을 내 위치로 초기화
    LaunchedEffect(initialLocation) {
        initialLocation?.let {
            if (state.pathPoints.isEmpty()) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(
                    it,
                    Constants.MAP_ZOOM
                )
            }
        }
    }

    // 카메라 이동
    LaunchedEffect(key1 = state.currentLocation) {
        // 사용자의 위치가 업데이트될 때마다, camera를 애니메이션으로 이동 처리
        state.currentLocation?.let {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(it, Constants.MAP_ZOOM)
            )
        }
    }

    // 화면 커짐 유지
    DisposableEffect(state.isTracking) {
        val window = (context as? android.app.Activity)?.window
        if(state.isTracking) {
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    RunScreen(
        state = state,
        onAction = viewModel::onAction,
        cameraPositionState = cameraPositionState
    )
}
