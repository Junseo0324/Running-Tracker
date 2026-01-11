package com.devhjs.runningtracker.core.util

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object LocationUtils {

    /**
     * 사용자가 위치 권한(Coarse 또는 FineLocation)을 승인했는지 확인하는 함수입니다.
     * @param context 애플리케이션 컨텍스트
     * @return 권한 승인 여부 (true: 승인됨, false: 거부됨)
     */
    fun hasLocationPermissions(context: Context): Boolean {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return hasFine || hasCoarse
    }
}
