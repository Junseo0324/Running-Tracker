package com.devhjs.runningtracker.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devhjs.runningtracker.domain.model.Run
import com.devhjs.runningtracker.presentation.components.AdMobBanner
import com.devhjs.runningtracker.presentation.components.CoupangPartnersCard
import com.devhjs.runningtracker.presentation.components.RouteImage
import com.devhjs.runningtracker.presentation.components.RouteMap
import com.devhjs.runningtracker.presentation.components.RunStatsSummary
import com.devhjs.runningtracker.presentation.designsystem.RunningBlack
import com.devhjs.runningtracker.presentation.designsystem.RunningDarkGrey
import com.devhjs.runningtracker.presentation.designsystem.TextGrey
import com.devhjs.runningtracker.presentation.designsystem.TextWhite
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * 저장된 러닝 기록 상세 화면. 결과 화면과 같은 구성이며 저장 버튼 대신 삭제 버튼이 있다.
 * 배너 광고만 하단에 고정하고 나머지는 함께 스크롤된다.
 */
@Composable
fun RunDetailScreen(
    state: RunDetailState = RunDetailState(),
    onAction: (RunDetailAction) -> Unit = {}
) {
    val dateFormat = remember { SimpleDateFormat("yyyy년 MM월 dd일 • a h:mm", Locale.KOREA) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RunningBlack)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                IconButton(
                    onClick = { onAction(RunDetailAction.OnBackClick) },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                }
                Text(
                    text = "러닝 기록",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                if (state.run != null) {
                    IconButton(
                        onClick = { onAction(RunDetailAction.OnDeleteClick) },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "기록 삭제", tint = TextGrey)
                    }
                }
            }

            val run = state.run
            when {
                run != null -> {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = remember(run.timestamp) { dateFormat.format(run.timestamp) },
                        color = TextGrey,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    RunStatsSummary(
                        distanceInMeters = run.distanceInMeters.toFloat(),
                        avgSpeedInKmh = run.avgSpeedInKMH,
                        timeInMillis = run.timeInMillis,
                        caloriesBurned = run.caloriesBurned
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    val routeModifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                    if (state.pathPoints.isNotEmpty()) {
                        RouteMap(pathPoints = state.pathPoints, modifier = routeModifier)
                    } else {
                        RouteImage(imageBytes = run.img, modifier = routeModifier)
                    }

                    state.coupangRecommendation?.let { recommendation ->
                        Spacer(modifier = Modifier.height(24.dp))
                        CoupangPartnersCard(
                            recommendation = recommendation,
                            onClick = { onAction(RunDetailAction.OnCoupangClick(recommendation.url)) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
                state.isLoaded -> {
                    Text(
                        text = "기록을 찾을 수 없어요.",
                        color = TextGrey,
                        modifier = Modifier.padding(top = 48.dp)
                    )
                }
            }
        }

        AdMobBanner(
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }

    if (state.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { onAction(RunDetailAction.OnDeleteDismiss) },
            title = { Text("기록 삭제") },
            text = { Text("이 러닝 기록을 삭제할까요? 삭제한 기록은 되돌릴 수 없어요.") },
            confirmButton = {
                TextButton(onClick = { onAction(RunDetailAction.OnDeleteConfirm) }) {
                    Text("삭제", color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(RunDetailAction.OnDeleteDismiss) }) {
                    Text("취소", color = TextWhite)
                }
            },
            containerColor = RunningDarkGrey,
            titleContentColor = TextWhite,
            textContentColor = TextGrey
        )
    }
}

@Preview
@Composable
private fun RunDetailScreenPreview() {
    RunDetailScreen(
        state = RunDetailState(
            run = Run(
                id = 1,
                timestamp = 1_700_000_000_000L,
                avgSpeedInKMH = 8.9f,
                distanceInMeters = 2_590,
                timeInMillis = 1_048_000L,
                caloriesBurned = 134
            ),
            isLoaded = true
        )
    )
}
