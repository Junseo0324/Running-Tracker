package com.devhjs.runningtracker.presentation.components


import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devhjs.runningtracker.core.util.ImageUtils
import com.devhjs.runningtracker.core.util.TimeUtils
import com.devhjs.runningtracker.domain.model.Run
import com.devhjs.runningtracker.presentation.designsystem.RunningGreen
import com.devhjs.runningtracker.presentation.designsystem.TextGrey
import com.devhjs.runningtracker.presentation.designsystem.TextWhite
import java.text.SimpleDateFormat
import java.util.Locale

private val THUMBNAIL_SIZE = 100.dp

@Composable
fun RunItemCard(run: Run) {
    val dateFormat = remember { SimpleDateFormat("MM월 dd일 • a h:mm", Locale.KOREA) }
    val dateString = remember(run.timestamp) { dateFormat.format(run.timestamp) }

    // 썸네일은 표시 크기에 맞춰 축소 디코딩하고, 리컴포지션마다 다시 만들지 않도록 기억해 둔다.
    // remember 가 없으면 매 리컴포지션마다 원본 크기의 비트맵이 새로 할당된다.
    val thumbnailSizePx = with(LocalDensity.current) { THUMBNAIL_SIZE.roundToPx() }
    val thumbnail = remember(run.img, thumbnailSizePx) {
        run.img?.let { bytes ->
            ImageUtils.decodeSampledBitmap(bytes, thumbnailSizePx, thumbnailSizePx)
                ?.asImageBitmap()
        }
    }

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
                    .size(THUMBNAIL_SIZE)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                thumbnail?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "Run Path",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}
