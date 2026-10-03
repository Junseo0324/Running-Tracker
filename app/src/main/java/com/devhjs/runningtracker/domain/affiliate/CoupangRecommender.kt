package com.devhjs.runningtracker.domain.affiliate

import com.devhjs.runningtracker.core.Constants

/** 표시 문구는 화면 쪽 문자열 리소스에서 카테고리별로 고른다. */
enum class CoupangCategory { RUNNING_SHOES, GEAR, NUTRITION, ELECTROLYTE, SOCKS, APPAREL, WATCH }

/** 카테고리에 연결된 파트너스 링크와 대표 상품 이미지. 비어있는 값은 미설정으로 본다. */
data class CoupangLink(
    val url: String,
    val imageUrl: String = ""
)

data class CoupangRecommendation(
    val category: CoupangCategory,
    val url: String,
    /** 대표 상품 이미지. 없으면 화면에서 카테고리 아이콘으로 대신한다. */
    val imageUrl: String? = null,
    /** 러닝화 교체 문구를 띄울 때의 누적 거리(km). 해당 없으면 null. */
    val shoeMileageKm: Long? = null
)

/**
 * 쿠팡 파트너스 추천 카드에 어떤 카테고리를 보여줄지 정한다.
 * - 쿠팡은 국내 배송이고 대가성 문구도 국내 법 기준이라 한국에서만 노출한다.
 * - 링크가 비어있는 카테고리는 결과에서 빠져 카드가 표시되지 않는다.
 */
object CoupangRecommender {
    private const val AVAILABLE_COUNTRY_CODE = "KR"

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

    fun isAvailableIn(countryCode: String): Boolean =
        countryCode.equals(AVAILABLE_COUNTRY_CODE, ignoreCase = true)

    fun forRun(
        distanceInMeters: Float,
        countryCode: String,
        linkOf: (CoupangCategory) -> CoupangLink = ::defaultLink
    ): CoupangRecommendation? {
        if (!isAvailableIn(countryCode)) return null
        val category = if (distanceInMeters >= LONG_RUN_METERS) CoupangCategory.NUTRITION else CoupangCategory.GEAR
        return build(category, linkOf)
    }

    fun forHistory(
        totalDistanceInMeters: Long,
        countryCode: String,
        linkOf: (CoupangCategory) -> CoupangLink = ::defaultLink
    ): List<CoupangRecommendation> {
        if (!isAvailableIn(countryCode)) return emptyList()
        val shoeMileageKm = (totalDistanceInMeters / 1000).takeIf { totalDistanceInMeters >= SHOE_REPLACE_METERS }
        val shoes = build(CoupangCategory.RUNNING_SHOES, linkOf)?.copy(shoeMileageKm = shoeMileageKm)
        val extras = HISTORY_EXTRAS.mapNotNull { build(it, linkOf) }
        return listOfNotNull(shoes) + extras
    }

    private fun build(
        category: CoupangCategory,
        linkOf: (CoupangCategory) -> CoupangLink
    ): CoupangRecommendation? {
        val link = linkOf(category)
        if (link.url.isBlank()) return null
        return CoupangRecommendation(
            category = category,
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
