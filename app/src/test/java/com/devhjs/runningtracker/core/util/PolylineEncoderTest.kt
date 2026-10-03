package com.devhjs.runningtracker.core.util

import com.google.android.gms.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PolylineEncoderTest {

    @Test
    fun `구글 문서의 예제 좌표를 같은 문자열로 인코딩한다`() {
        // https://developers.google.com/maps/documentation/utilities/polylinealgorithm
        val points = listOf(
            LatLng(38.5, -120.2),
            LatLng(40.7, -120.95),
            LatLng(43.252, -126.453)
        )

        assertEquals("_p~iF~ps|U_ulLnnqC_mqNvxq`@", PolylineEncoder.encode(points))
    }

    @Test
    fun `인코딩한 뒤 복원하면 소수점 5자리까지 같은 좌표가 나온다`() {
        val points = listOf(
            LatLng(37.56651, 126.97801),
            LatLng(37.56662, 126.97790),
            LatLng(37.56640, 126.97815)
        )

        val decoded = PolylineEncoder.decode(PolylineEncoder.encode(points))

        assertEquals(points.size, decoded.size)
        points.zip(decoded).forEach { (expected, actual) ->
            assertEquals(expected.latitude, actual.latitude, 1e-5)
            assertEquals(expected.longitude, actual.longitude, 1e-5)
        }
    }

    @Test
    fun `일시정지로 끊긴 여러 구간을 그대로 복원한다`() {
        val path = listOf(
            listOf(LatLng(37.5, 127.0), LatLng(37.501, 127.001)),
            listOf(LatLng(37.502, 127.002))
        )

        val decoded = PolylineEncoder.decodePath(PolylineEncoder.encodePath(path))

        assertEquals(listOf(2, 1), decoded.map { it.size })
        assertEquals(37.502, decoded[1][0].latitude, 1e-5)
    }

    @Test
    fun `빈 구간은 버리고 빈 경로는 빈 문자열이 된다`() {
        assertEquals("", PolylineEncoder.encodePath(emptyList()))
        assertEquals("", PolylineEncoder.encodePath(listOf(emptyList())))
        assertTrue(PolylineEncoder.decodePath("").isEmpty())
    }

    @Test
    fun `1시간 분량 좌표도 좌표당 수 바이트로 압축된다`() {
        val points = List(3_600) { i -> LatLng(37.5 + i * 0.00003, 127.0 + i * 0.00002) }

        val encoded = PolylineEncoder.encode(points)

        // 좌표당 평균 8자 미만 (JSON 이면 40자 안팎)
        assertTrue("길이: ${encoded.length}", encoded.length < 3_600 * 8)
    }
}
