package com.devhjs.runningtracker.presentation.result


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devhjs.runningtracker.presentation.components.AdMobBanner
import com.devhjs.runningtracker.presentation.components.CoupangPartnersCard
import com.devhjs.runningtracker.presentation.components.PrimaryButton
import com.devhjs.runningtracker.presentation.components.RouteMap
import com.devhjs.runningtracker.presentation.components.RunStatsSummary
import com.devhjs.runningtracker.presentation.designsystem.RunningBlack
import com.devhjs.runningtracker.presentation.designsystem.TextWhite

/**
 * 운동 결과 화면.
 * 배너 광고만 하단에 고정하고, 나머지(기록 · 지도 · 저장 버튼 · 추천)는 함께 스크롤된다.
 */
@Composable
fun ResultScreen(
    state: ResultState = ResultState(),
    onAction: (ResultAction) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RunningBlack)
            .systemBarsPadding()
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
                    onClick = { onAction(ResultAction.OnDiscardClick) },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextWhite)
                }
                Text(
                    text = "운동 결과",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            RunStatsSummary(
                distanceInMeters = state.distanceInMeters,
                avgSpeedInKmh = state.avgSpeed,
                timeInMillis = state.timeInMillis,
                caloriesBurned = state.caloriesBurned
            )

            Spacer(modifier = Modifier.height(24.dp))

            RouteMap(
                pathPoints = state.pathPoints,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            PrimaryButton(
                text = "기록 저장",
                onClick = { onAction(ResultAction.OnSaveClick) }
            )

            state.coupangRecommendation?.let { recommendation ->
                Spacer(modifier = Modifier.height(16.dp))
                CoupangPartnersCard(
                    recommendation = recommendation,
                    onClick = { onAction(ResultAction.OnCoupangClick(recommendation.url)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        AdMobBanner(
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ResultScreenPreview() {
    ResultScreen()
}
