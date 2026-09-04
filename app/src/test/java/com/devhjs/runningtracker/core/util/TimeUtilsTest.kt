package com.devhjs.runningtracker.core.util

import org.junit.Assert.assertEquals
import org.junit.Test

class TimeUtilsTest {

    @Test
    fun `0ms는 00시 00분 00초로 포맷된다`() {
        assertEquals("00:00:00", TimeUtils.getFormattedStopWatchTime(0L))
    }

    @Test
    fun `한 자리 수 시분초는 0으로 패딩된다`() {
        // 1시간 2분 3초
        val ms = (1 * 3600 + 2 * 60 + 3) * 1000L

        assertEquals("01:02:03", TimeUtils.getFormattedStopWatchTime(ms))
    }

    @Test
    fun `두 자리 수 시분초는 패딩 없이 그대로 출력된다`() {
        // 12시간 34분 56초
        val ms = (12 * 3600 + 34 * 60 + 56) * 1000L

        assertEquals("12:34:56", TimeUtils.getFormattedStopWatchTime(ms))
    }

    @Test
    fun `1초 미만은 초 단위에서 버려진다`() {
        assertEquals("00:00:00", TimeUtils.getFormattedStopWatchTime(999L))
    }

    @Test
    fun `includeMillis가 true면 10ms 단위가 뒤에 붙는다`() {
        // 1초 230밀리초 -> 밀리초는 10으로 나눠 23
        assertEquals("00:00:01:23", TimeUtils.getFormattedStopWatchTime(1_230L, includeMillis = true))
    }

    @Test
    fun `includeMillis에서 10ms 미만 값도 0으로 패딩된다`() {
        // 1초 50밀리초 -> 50 / 10 = 5
        assertEquals("00:00:01:05", TimeUtils.getFormattedStopWatchTime(1_050L, includeMillis = true))
    }

    @Test
    fun `24시간을 넘어도 일 단위로 넘어가지 않고 시간이 누적된다`() {
        val ms = 25 * 3600 * 1000L

        assertEquals("25:00:00", TimeUtils.getFormattedStopWatchTime(ms))
    }
}
