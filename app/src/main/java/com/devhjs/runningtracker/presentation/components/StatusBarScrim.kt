package com.devhjs.runningtracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 지도 위에서 상태바 아이콘(시계 · 배터리)이 지도 글자와 겹쳐 안 보이지 않도록
 * 상태바 뒤에 위에서 아래로 옅어지는 흰색 그라데이션을 깐다.
 */
@Composable
fun StatusBarScrim(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Color.White.copy(alpha = 0.85f),
                    0.6f to Color.White.copy(alpha = 0.5f),
                    1f to Color.Transparent
                )
            )
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Spacer(modifier = Modifier.height(24.dp))
    }
}
