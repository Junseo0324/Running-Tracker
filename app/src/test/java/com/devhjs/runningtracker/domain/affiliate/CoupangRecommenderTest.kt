package com.devhjs.runningtracker.domain.affiliate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoupangRecommenderTest {

    private val links: (CoupangCategory) -> CoupangLink = {
        CoupangLink(url = "https://link.coupang.com/a/${it.name}")
    }

    @Test
    fun `5km 미만 러닝은 러닝 벨트를 추천한다`() {
        val result = CoupangRecommender.forRun(3_000f, links)

        assertEquals(CoupangCategory.GEAR, result?.category)
        assertEquals("https://link.coupang.com/a/GEAR", result?.url)
    }

    @Test
    fun `5km 이상 러닝은 에너지젤을 추천한다`() {
        val result = CoupangRecommender.forRun(5_000f, links)

        assertEquals(CoupangCategory.NUTRITION, result?.category)
    }

    @Test
    fun `기록 화면은 러닝화를 맨 앞에 두고 모든 카테고리를 보여준다`() {
        val result = CoupangRecommender.forHistory(0L, links)

        assertEquals(CoupangCategory.RUNNING_SHOES, result.first().category)
        assertEquals(CoupangCategory.entries.toSet(), result.map { it.category }.toSet())
    }

    @Test
    fun `누적 500km 이상이면 러닝화 교체 문구를 보여준다`() {
        val shoes = CoupangRecommender.forHistory(523_400L, links).first()

        assertTrue(shoes.message.contains("523km"))
    }

    @Test
    fun `누적 500km 미만이면 일반 러닝화 문구를 보여준다`() {
        val shoes = CoupangRecommender.forHistory(120_000L, links).first()

        assertEquals(CoupangCategory.RUNNING_SHOES.message, shoes.message)
    }

    @Test
    fun `링크가 비어있는 카테고리는 빠진다`() {
        val onlySocks: (CoupangCategory) -> CoupangLink = {
            CoupangLink(url = if (it == CoupangCategory.SOCKS) "https://x" else "")
        }

        assertNull(CoupangRecommender.forRun(3_000f, onlySocks))
        assertEquals(listOf(CoupangCategory.SOCKS), CoupangRecommender.forHistory(0L, onlySocks).map { it.category })
    }

    @Test
    fun `이미지 주소가 있으면 함께 전달하고 비어있으면 null 이다`() {
        val withImage: (CoupangCategory) -> CoupangLink = {
            CoupangLink(url = "https://x", imageUrl = if (it == CoupangCategory.GEAR) "https://img" else " ")
        }

        assertEquals("https://img", CoupangRecommender.forRun(3_000f, withImage)?.imageUrl)
        assertNull(CoupangRecommender.forRun(5_000f, withImage)?.imageUrl)
    }
}
