package com.devhjs.runningtracker.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devhjs.runningtracker.presentation.components.FullScreenMap
import com.devhjs.runningtracker.presentation.components.PrimaryButton
import com.devhjs.runningtracker.ui.theme.RunningBlack
import com.devhjs.runningtracker.ui.theme.RunningDarkGrey
import com.devhjs.runningtracker.ui.theme.RunningGreen
import com.devhjs.runningtracker.ui.theme.TextGrey
import com.devhjs.runningtracker.ui.theme.TextWhite


@Composable
fun HomeScreen(
    state: HomeState = HomeState(),
    onAction: (HomeAction) -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        FullScreenMap(
            isMyLocationEnabled = state.isPermissionGranted,
            isMyLocationButtonEnabled = true,
            currentLocation = state.currentLocation
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if(state.isPermissionGranted) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                     Box(
                        modifier = Modifier
                            .background(
                                RunningDarkGrey.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = RunningGreen,
                                modifier = Modifier
                                    .width(16.dp)
                                    .height(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GPS 연결됨",
                                color = TextWhite,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = RunningBlack.copy(alpha = 0.9f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "오늘도 달려볼까요?",
                        color = TextWhite,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 8.dp)
                    )
                     Text(
                        text = "준비가 되면 시작 버튼을 눌러주세요.",
                        color = TextGrey,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .align(Alignment.Start)
                            .padding(bottom = 24.dp)
                    )

                    PrimaryButton(
                        text = "운동 시작",
                        onClick = {
                            onAction(HomeAction.OnStartClick)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    HomeScreen()
}