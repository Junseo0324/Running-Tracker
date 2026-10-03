package com.devhjs.runningtracker.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.devhjs.runningtracker.core.Constants.COUPANG_PARTNERS_DISCLOSURE
import com.devhjs.runningtracker.domain.affiliate.CoupangCategory
import com.devhjs.runningtracker.domain.affiliate.CoupangRecommendation
import com.devhjs.runningtracker.presentation.designsystem.RunningDarkGrey
import com.devhjs.runningtracker.presentation.designsystem.RunningGreen
import com.devhjs.runningtracker.presentation.designsystem.RunningWhite
import com.devhjs.runningtracker.presentation.designsystem.TextGrey
import com.devhjs.runningtracker.presentation.designsystem.TextWhite

/**
 * 쿠팡 파트너스 추천 카드.
 * 대가성 문구는 공정위 지침상 링크와 함께 항상 보여야 하므로 카드에 포함한다.
 */
@Composable
fun CoupangPartnersCard(
    recommendation: CoupangRecommendation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(RunningDarkGrey)
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val imageUrl = recommendation.imageUrl
            if (imageUrl != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(RunningWhite)
                ) {
                    ProductImage(imageUrl, recommendation.title)
                }
            } else {
                CategoryBadge(recommendation.category)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recommendation.title,
                    color = TextWhite,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(text = recommendation.message, color = TextGrey, fontSize = 13.sp)
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "쿠팡에서 보기",
                tint = TextGrey
            )
        }
        CoupangDisclosure()
    }
}

/**
 * 여러 카테고리를 가로로 넘겨 보는 추천 목록. 기록 화면처럼 세로 공간을 아껴야 할 때 쓴다.
 */
@Composable
fun CoupangPartnersRow(
    recommendations: List<CoupangRecommendation>,
    onClick: (CoupangRecommendation) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "러닝 용품 추천",
            color = TextWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(recommendations, key = { it.category }) { recommendation ->
                Column(
                    modifier = Modifier
                        .width(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(RunningDarkGrey)
                        .clickable { onClick(recommendation) }
                ) {
                    val imageUrl = recommendation.imageUrl
                    // 이미지 유무와 상관없이 타일 높이를 맞추기 위해 같은 크기의 영역을 둔다.
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                if (imageUrl != null) RunningWhite else RunningGreen.copy(alpha = 0.06f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUrl != null) {
                            ProductImage(imageUrl, recommendation.title)
                        } else {
                            CategoryBadge(recommendation.category)
                        }
                    }
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = recommendation.title,
                            color = TextWhite,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = recommendation.message,
                            color = TextGrey,
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            minLines = 2,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
        CoupangDisclosure()
    }
}

/**
 * 파트너스가 주는 이미지는 120x240 세로 배너(로고 · 상품 사진 · 상품명 · 버튼)라서
 * 그대로 넣으면 상품이 너무 작다. 가로를 꽉 채우고 상품 사진이 있는 위쪽 부분만 보이게 자른다.
 */
@Composable
private fun ProductImage(imageUrl: String, title: String) {
    AsyncImage(
        model = imageUrl,
        contentDescription = title,
        contentScale = ContentScale.FillWidth,
        alignment = BannerProductAlignment,
        modifier = Modifier.fillMaxSize()
    )
}

/** 배너에서 상품 사진은 대략 세로 15~60% 구간에 있다. 그 구간이 보이도록 위쪽으로 치우쳐 정렬한다. */
private val BannerProductAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = -0.5f)

/** 카테고리를 한눈에 구분할 수 있도록 아이콘을 연한 초록 원 안에 보여준다. */
@Composable
private fun CategoryBadge(category: CoupangCategory) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(RunningGreen.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = category.icon(),
            contentDescription = null,
            tint = RunningGreen,
            modifier = Modifier.size(22.dp)
        )
    }
}

private fun CoupangCategory.icon(): ImageVector = when (this) {
    CoupangCategory.RUNNING_SHOES -> Icons.AutoMirrored.Filled.DirectionsRun
    CoupangCategory.GEAR -> BeltIcon
    CoupangCategory.NUTRITION -> Icons.Default.Bolt
    CoupangCategory.ELECTROLYTE -> Icons.Default.WaterDrop
    CoupangCategory.SOCKS -> SockIcon
    CoupangCategory.APPAREL -> Icons.Default.Checkroom
    CoupangCategory.WATCH -> Icons.Default.Watch
}

/** Material 아이콘에 러닝 벨트가 없어 같은 24dp 그리드로 직접 그린 아이콘. */
private val BeltIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "RunningBelt",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // 양옆 허리끈
        addPath(pathData = addPathNodes("M1,11h5v2H1z"), fill = SolidColor(Color.Black))
        addPath(pathData = addPathNodes("M18,11h5v2h-5z"), fill = SolidColor(Color.Black))
        // 가운데 파우치 (지퍼 부분은 비워서 표현)
        addPath(
            pathData = addPathNodes(
                "M9,7h6a3,3 0 0 1 3,3v4a3,3 0 0 1 -3,3h-6a3,3 0 0 1 -3,-3v-4a3,3 0 0 1 3,-3z" +
                    "M8.5,9.5v1.5h7v-1.5z"
            ),
            pathFillType = PathFillType.EvenOdd,
            fill = SolidColor(Color.Black)
        )
    }.build()
}

/** Material 아이콘에 양말이 없어 같은 24dp 그리드로 직접 그린 아이콘. */
private val SockIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Sock",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // 발목 밴드
        addPath(pathData = addPathNodes("M7,2h7v2H7z"), fill = SolidColor(Color.Black))
        // 다리 + 발 (오른쪽이 발끝)
        addPath(
            pathData = addPathNodes("M7,5H14V12H18.5A3.5,3.5 0 0 1 18.5,19H10A3,3 0 0 1 7,16Z"),
            fill = SolidColor(Color.Black)
        )
    }.build()
}

/** 공정위 지침상 파트너스 링크와 함께 항상 보여야 하는 대가성 문구. */
@Composable
private fun CoupangDisclosure() {
    Text(
        text = COUPANG_PARTNERS_DISCLOSURE,
        color = TextGrey,
        fontSize = 10.sp,
        lineHeight = 13.sp,
        modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
    )
}

@Preview
@Composable
private fun CoupangPartnersCardPreview() {
    CoupangPartnersCard(
        recommendation = CoupangRecommendation(
            category = CoupangCategory.RUNNING_SHOES,
            title = "러닝화",
            message = "누적 523km 달성! 러닝화를 점검해볼 때예요",
            url = ""
        ),
        onClick = {}
    )
}

@Preview
@Composable
private fun CoupangPartnersRowPreview() {
    CoupangPartnersRow(
        recommendations = CoupangCategory.entries.map {
            CoupangRecommendation(it, it.title, it.message, url = "")
        },
        onClick = {}
    )
}
