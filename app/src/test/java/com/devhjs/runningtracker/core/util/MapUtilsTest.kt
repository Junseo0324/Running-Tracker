package com.devhjs.runningtracker.core.util

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * [MapUtils]는 android.location.Location.distanceBetween() 에 의존하므로
 * 순수 JVM 테스트에서는 해당 static 메서드를 MockK로 스텁 처리한다.
 *
 * 따라서 이 테스트가 검증하는 것은 실제 측지 거리 계산의 정확도가 아니라,
 * 폴리라인을 인접한 좌표 쌍으로 올바르게 순회하며 결과를 누적하는지다.
 */
class MapUtilsTest {

    /** 스텁이 각 구간마다 반환할 거리 (미터) */
    private val distancePerSegment = 10f

    @Before
    fun setUp() {
        mockkStatic(Location::class)
        every {
            Location.distanceBetween(any(), any(), any(), any(), any())
        } answers {
            // 5번째 인자(results 배열)에 거리를 써넣는 방식으로 동작한다
            val results = arg<FloatArray>(4)
            results[0] = distancePerSegment
        }
    }

    @After
    fun tearDown() {
        unmockkStatic(Location::class)
    }

    @Test
    fun `빈 폴리라인의 길이는 0이다`() {
        val result = MapUtils.calculatePolylineLength(emptyList())

        assertEquals(0f, result, 0f)
        verify(exactly = 0) { Location.distanceBetween(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `좌표가 1개뿐이면 구간이 없으므로 길이는 0이다`() {
        val result = MapUtils.calculatePolylineLength(listOf(LatLng(37.0, 127.0)))

        assertEquals(0f, result, 0f)
        verify(exactly = 0) { Location.distanceBetween(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `좌표가 2개면 구간 1개의 거리를 반환한다`() {
        val polyline = listOf(LatLng(37.0, 127.0), LatLng(37.1, 127.1))

        val result = MapUtils.calculatePolylineLength(polyline)

        assertEquals(distancePerSegment, result, 0.001f)
        verify(exactly = 1) { Location.distanceBetween(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `좌표가 N개면 N-1개 구간의 거리가 누적된다`() {
        val polyline = listOf(
            LatLng(37.0, 127.0),
            LatLng(37.1, 127.1),
            LatLng(37.2, 127.2),
            LatLng(37.3, 127.3)
        )

        val result = MapUtils.calculatePolylineLength(polyline)

        assertEquals(distancePerSegment * 3, result, 0.001f)
        verify(exactly = 3) { Location.distanceBetween(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `인접한 두 좌표를 순서대로 전달한다`() {
        val first = LatLng(37.0, 127.0)
        val second = LatLng(37.5, 127.5)

        MapUtils.calculatePolylineLength(listOf(first, second))

        verify(exactly = 1) {
            Location.distanceBetween(
                first.latitude,
                first.longitude,
                second.latitude,
                second.longitude,
                any()
            )
        }
    }
}
