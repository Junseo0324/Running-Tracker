package com.devhjs.runningtracker.presentation.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 화면별로 상태바 · 내비게이션 바 아이콘 색을 맞춘다.
 *
 * 앱은 edge-to-edge 로 그려져 시스템 바 뒤로 화면 배경이 비치므로,
 * 밝은 배경(지도)에서는 어두운 아이콘, 어두운 배경에서는 밝은 아이콘이어야 보인다.
 * 이전 값으로 되돌리지 않고 각 화면이 자기 값을 선언하는 방식이라 화면 전환 순서와 무관하다.
 */
@Composable
fun SystemBarIcons(darkIcons: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = view.context.findActivity()?.window ?: return@SideEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = darkIcons
            isAppearanceLightNavigationBars = darkIcons
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
