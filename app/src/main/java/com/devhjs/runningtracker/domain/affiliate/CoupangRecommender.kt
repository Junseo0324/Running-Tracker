package com.devhjs.runningtracker.domain.affiliate

import com.devhjs.runningtracker.core.Constants

enum class CoupangCategory(val title: String, val message: String) {
    RUNNING_SHOES("러닝화", "발에 맞는 러닝화로 부상을 줄여보세요"),
    GEAR("러닝 벨트 · 암밴드", "휴대폰을 흔들림 없이 들고 달려보세요"),
    NUTRITION("에너지젤", "5km 넘게 달렸다면 에너지 보충도 챙겨보세요"),
    ELECTROLYTE("전해질 · 이온음료", "땀으로 빠진 수분과 전해질을 채워보세요"),
    SOCKS("러닝 양말", "물집과 쓸림을 줄여줘요"),
    APPAREL("러닝복", "가볍고 땀이 잘 마르는 기능성 의류"),
    WATCH("스마트워치", "페이스와 심박을 손목에서 바로 확인해요")
}

/** 카테고리에 연결된 파트너스 링크와 대표 상품 이미지. 비어있는 값은 미설정으로 본다. */
data class CoupangLink(
    val url: String,
    val imageUrl: String = ""
)

data class CoupangRecommendation(
    val category: CoupangCategory,
    val title: String,
    val message: String,
    val url: String,
    /** 대표 상품 이미지. 없으면 화면에서 카테고리 아이콘으로 대신한다. */
    val imageUrl: String? = null
)

/**
 * 쿠팡 파트너스 추천 카드에 어떤 카테고리를 보여줄지 정한다.
 * 링크가 비어있는 카테고리는 결과에서 빠져 카드가 표시되지 않는다.
 */
object CoupangRecommender {
    /** 이 거리 이상 달린 날은 에너지젤을 추천한다. */
    const val LONG_RUN_METERS = 5_000f

    /** 러닝화 수명은 보통 500~800km 로 본다. 누적 거리가 이를 넘으면 교체 문구를 띄운다. */
    const val SHOE_REPLACE_METERS = 500_000L

    /** 기록 화면에서 러닝화 다음으로 보여줄 카테고리 순서. */
    private val HISTORY_EXTRAS = listOf(
        CoupangCategory.GEAR,
        CoupangCategory.SOCKS,
        CoupangCategory.ELECTROLYTE,
        CoupangCategory.NUTRITION,
        CoupangCategory.APPAREL,
        CoupangCategory.WATCH
    )

    fun forRun(
        distanceInMeters: Float,
        linkOf: (CoupangCategory) -> CoupangLink = ::defaultLink
    ): CoupangRecommendation? {
        val category = if (distanceInMeters >= LONG_RUN_METERS) CoupangCategory.NUTRITION else CoupangCategory.GEAR
        return build(category, category.message, linkOf)
    }

    fun forHistory(
        totalDistanceInMeters: Long,
        linkOf: (CoupangCategory) -> CoupangLink = ::defaultLink
    ): List<CoupangRecommendation> {
        val shoeMessage = if (totalDistanceInMeters >= SHOE_REPLACE_METERS) {
            "누적 ${totalDistanceInMeters / 1000}km 달성! 러닝화를 점검해볼 때예요"
        } else {
            CoupangCategory.RUNNING_SHOES.message
        }
        val shoes = build(CoupangCategory.RUNNING_SHOES, shoeMessage, linkOf)
        val extras = HISTORY_EXTRAS.mapNotNull { build(it, it.message, linkOf) }
        return listOfNotNull(shoes) + extras
    }

    private fun build(
        category: CoupangCategory,
        message: String,
        linkOf: (CoupangCategory) -> CoupangLink
    ): CoupangRecommendation? {
        val link = linkOf(category)
        if (link.url.isBlank()) return null
        return CoupangRecommendation(
            category = category,
            title = category.title,
            message = message,
            url = link.url,
            imageUrl = link.imageUrl.ifBlank { null }
        )
    }

    private fun defaultLink(category: CoupangCategory): CoupangLink = when (category) {
        CoupangCategory.RUNNING_SHOES ->
            CoupangLink(Constants.COUPANG_LINK_RUNNING_SHOES, Constants.COUPANG_IMAGE_RUNNING_SHOES)
        CoupangCategory.GEAR ->
            CoupangLink(Constants.COUPANG_LINK_GEAR, Constants.COUPANG_IMAGE_GEAR)
        CoupangCategory.NUTRITION ->
            CoupangLink(Constants.COUPANG_LINK_NUTRITION, Constants.COUPANG_IMAGE_NUTRITION)
        CoupangCategory.ELECTROLYTE ->
            CoupangLink(Constants.COUPANG_LINK_ELECTROLYTE, Constants.COUPANG_IMAGE_ELECTROLYTE)
        CoupangCategory.SOCKS ->
            CoupangLink(Constants.COUPANG_LINK_SOCKS, Constants.COUPANG_IMAGE_SOCKS)
        CoupangCategory.APPAREL ->
            CoupangLink(Constants.COUPANG_LINK_APPAREL, Constants.COUPANG_IMAGE_APPAREL)
        CoupangCategory.WATCH ->
            CoupangLink(Constants.COUPANG_LINK_WATCH, Constants.COUPANG_IMAGE_WATCH)
    }
}
