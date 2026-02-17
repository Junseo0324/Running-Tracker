package com.devhjs.runningtracker.presentation.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()

    // 앱 사용 중 필요한 위치 권한 목록
    val foregroundPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    // permissions 의 승인 상태를 기다리는 객체
    val foregroundPermissionState = rememberMultiplePermissionsState(permissions = foregroundPermissions)

    // Android 13(TIRAMISU) 이상에서 알림 권한 요청
    val notificationPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        null
    }


    LaunchedEffect(true) {
        viewModel.event.collect { event ->
            when(event) {
                is HomeEvent.Navigate -> onNavigate(event.route)
            }
        }
    }

    // Coarse(대략적) 혹은 Fine(정확한) 위치 권한 중 하나라도 있으면 허용으로 간주
    LaunchedEffect(foregroundPermissionState.permissions) {
        val isForegroundGranted = foregroundPermissionState.permissions.any { it.status.isGranted }
        viewModel.onAction(HomeAction.OnPermissionsResult(isForegroundGranted))
    }


    HomeScreen(
        state = state,
        onAction = { action ->
            when(action) {
                HomeAction.OnStartClick -> {
                    // gps 체크
                    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

                    if(!isGpsEnabled) {
                        Toast.makeText(context, "GPS를 켜주세요.", Toast.LENGTH_SHORT).show()
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                        return@HomeScreen
                    }

                    // 위치 권한 확인
                    if (!foregroundPermissionState.permissions.any { it.status.isGranted }) {
                        foregroundPermissionState.launchMultiplePermissionRequest()
                        return@HomeScreen
                    }

                    // 알림 권한 확인
                    if (notificationPermissionState != null && !notificationPermissionState.status.isGranted) {
                        notificationPermissionState.launchPermissionRequest()
                        return@HomeScreen
                    }

                    // 모든 권한이 있으면 시작
                    viewModel.onAction(action)
                }
                else -> viewModel.onAction(action)
            }
        },
    )
}
