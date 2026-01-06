package com.devhjs.runningtracker.presentation.home

import android.Manifest
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


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreenRoot(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsStateWithLifecycle()
    
    val foregroundPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val foregroundPermissionState = rememberMultiplePermissionsState(permissions = foregroundPermissions)

    val backgroundPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
         listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    } else {
        emptyList()
    }
    val backgroundPermissionState = rememberMultiplePermissionsState(permissions = backgroundPermission)


    LaunchedEffect(true) {
        viewModel.event.collect { event ->
            when(event) {
                is HomeEvent.Navigate -> onNavigate(event.route)
            }
        }
    }

    LaunchedEffect(foregroundPermissionState.permissions, backgroundPermissionState.allPermissionsGranted) {
        // Coarse(대략적) 혹은 Fine(정확한) 위치 권한 중 하나라도 있으면 허용으로 간주
        val isForegroundGranted = foregroundPermissionState.permissions.any { it.status.isGranted }
        val isBackgroundGranted = if (backgroundPermission.isNotEmpty()) backgroundPermissionState.allPermissionsGranted else true
        
        // 엄밀히는 둘 다 있어야 "완벽한 허용"이지만, 앱 사용에는 Foreground만 있어도 "위치 표시"는 됨.
        // 하지만 트래킹을 위해서는 Background가 필요.
        // ViewModel에는 "권한이 충분한가"를 전달.
        // viewModel에게는 Foreground만 있어도 위치 표시는 가능하므로 Foreground 여부 전달
        viewModel.onAction(HomeAction.OnPermissionsResult(isForegroundGranted))
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
                        Toast.makeText(context, "GPS를 켜주세요.", Toast.LENGTH_SHORT).show()
                        val intent = android.content.Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                        return@HomeScreen
                    }

                // 2. Check Permissions
                    // 하나라도 권한이 있으면 통과
                    if (foregroundPermissionState.permissions.any { it.status.isGranted }) {
                        viewModel.onAction(action)
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
