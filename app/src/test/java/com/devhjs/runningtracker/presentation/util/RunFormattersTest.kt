package com.devhjs.runningtracker.presentation.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class RunFormattersTest {

    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `거리는 지정한 소수 자릿수로 km 단위 표시한다`() {
        assertEquals("5.26", formatDistanceKm(5_255f, fractionDigits = 2))
        assertEquals("12.0", formatDistanceKm(12_000f, fractionDigits = 1))
    }

    @Test
    fun `아랍어 기기에서도 거리는 일반 숫자와 점 소수점으로 표시한다`() {
        Locale.setDefault(Locale.forLanguageTag("ar-QA"))

        assertEquals("5.26", formatDistanceKm(5_255f, fractionDigits = 2))
    }
}
