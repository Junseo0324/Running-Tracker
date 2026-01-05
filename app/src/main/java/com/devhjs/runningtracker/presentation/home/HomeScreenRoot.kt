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


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    // 1. Foreground Permissions (Used first)
    val foregroundPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val foregroundPermissionState = rememberMultiplePermissionsState(permissions = foregroundPermissions)

    // 2. Background Permission (Requested only if needed, usually for Service)
    // Android Q (29) 이상부터 백그라운드 권한 필요.
    // Android R (30) 이상부터는 Foreground 승인 후 별도 요청 필요.
    val backgroundPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
         listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        emptyList()
    }
    // Background permission state (only relevant if list is not empty)
    val backgroundPermissionState = rememberMultiplePermissionsState(permissions = backgroundPermission)


    LaunchedEffect(true) {
        viewModel.event.collect { event ->
            when(event) {
                is HomeEvent.Navigate -> onNavigate(event.route)
            }
        }
    }

    // 초기 진입 시 권한 상태를 ViewModel에 알림 (둘 다 체크)
    LaunchedEffect(foregroundPermissionState.allPermissionsGranted, backgroundPermissionState.allPermissionsGranted) {
        val isForegroundGranted = foregroundPermissionState.allPermissionsGranted
        val isBackgroundGranted = if (backgroundPermission.isNotEmpty()) backgroundPermissionState.allPermissionsGranted else true
        
        // 엄밀히는 둘 다 있어야 "완벽한 허용"이지만, 앱 사용에는 Foreground만 있어도 "위치 표시"는 됨.
        // 하지만 트래킹을 위해서는 Background가 필요.
        // ViewModel에는 "권한이 충분한가"를 전달.
        viewModel.onAction(HomeAction.OnPermissionsResult(isForegroundGranted && isBackgroundGranted))
    }


    HomeScreen(
        state = state,
        onAction = { action ->
            when(action) {
                HomeAction.OnStartClick -> {
                    // 1. Check GPS
                    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                    val isGpsEnabled = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                            locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

                    if(!isGpsEnabled) {
                        android.widget.Toast.makeText(context, "GPS를 켜주세요.", android.widget.Toast.LENGTH_SHORT).show()
                        // 굳이 Intent로 설정창 보낼 수도 있음 (선택사항)
                        val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                        return@HomeScreen
                    }

                    // 2. Check Permissions
                    if (foregroundPermissionState.allPermissionsGranted) {
                        // Foreground OK. Check Background (if needed)
                        if (backgroundPermission.isNotEmpty() && !backgroundPermissionState.allPermissionsGranted) {
                            // Request Background
                             backgroundPermissionState.launchMultiplePermissionRequest()
                        } else {
                            // All OK
                            viewModel.onAction(action)
                        }
                    } else {
                        // Request Foreground
                        foregroundPermissionState.launchMultiplePermissionRequest()
                    }
                }
                else -> viewModel.onAction(action)
            }
        },
    )
}
