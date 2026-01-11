package com.devhjs.runningtracker.presentation.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.devhjs.runningtracker.core.util.ImageUtils
import com.devhjs.runningtracker.core.util.TimeUtils
import com.devhjs.runningtracker.domain.model.Run
import com.devhjs.runningtracker.ui.theme.RunningBlack
import com.devhjs.runningtracker.ui.theme.RunningGreen
import com.devhjs.runningtracker.ui.theme.TextGrey
import com.devhjs.runningtracker.ui.theme.TextWhite
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RunHistoryScreenRoot(
    viewModel: RunHistoryViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit
) {
    val runs by viewModel.runs.collectAsStateWithLifecycle()
    RunHistoryScreen(
        runs = runs,
        onNavigateUp = onNavigateUp
    )
}

@Composable
fun RunHistoryScreen(
    runs: List<Run>,
    onNavigateUp: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    androidx.compose.runtime.LaunchedEffect(Unit) {
        com.devhjs.runningtracker.presentation.util.AdHelper.showInterstitial(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RunningBlack)
            .padding(16.dp)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateUp) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextWhite)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "저장된 러닝",
                color = TextWhite,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(runs) { run ->
                RunItemCard(run)
            }
        }
        
        com.devhjs.runningtracker.presentation.components.AdMobBanner()
    }
}

@Composable
fun RunItemCard(run: Run) {
    val dateFormat = SimpleDateFormat("MM월 dd일 • a h:mm", Locale.KOREA)
    val dateString = dateFormat.format(run.timestamp)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Stats
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dateString,
                    color = TextGrey,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "${String.format("%.1f", run.distanceInMeters / 1000f)}",
                        color = TextWhite,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "km",
                        color = RunningGreen,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    Column {
                        Text(text = "시간", color = TextGrey, fontSize = 12.sp)
                        Text(
                            text = TimeUtils.getFormattedStopWatchTime(run.timeInMillis),
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column {
                        Text(text = "칼로리", color = TextGrey, fontSize = 12.sp)
                        Text(
                            text = "${run.caloriesBurned}",
                            color = TextWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Right: Image
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                run.img?.let {
                    Image(
                        bitmap = ImageUtils.bytesToBitmap(it).asImageBitmap(),
                        contentDescription = "Run Path",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
