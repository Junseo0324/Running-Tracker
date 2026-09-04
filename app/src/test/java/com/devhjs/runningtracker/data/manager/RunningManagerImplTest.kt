package com.devhjs.runningtracker.data.manager

import android.location.Location
import app.cash.turbine.test
import com.devhjs.runningtracker.data.local.SerializableLatLng
import com.devhjs.runningtracker.data.local.TempRunState
import com.devhjs.runningtracker.fake.FakeGpsStatusClient
import com.devhjs.runningtracker.fake.FakeTempRunDataSource
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [RunningManagerImpl]은 이 앱에서 실질적인 UseCase 역할을 하는 도메인 로직 보유자다.
 * 러닝 세션의 상태(추적 여부 / 경로 / 시간)와 프로세스 사망 대비 임시 저장을 책임진다.
 */
class RunningManagerImplTest {

    private lateinit var gpsStatusClient: FakeGpsStatusClient
    private lateinit var tempRunDataSource: FakeTempRunDataSource

    @Before
    fun setUp() {
        gpsStatusClient = FakeGpsStatusClient(initialEnabled = true)
        tempRunDataSource = FakeTempRunDataSource()
    }

    /**
     * isGpsEnabled 가 SharingStarted.Eagerly 로 무한 수집하므로,
     * runTest 가 끝날 때 자동 취소되는 backgroundScope 를 외부 스코프로 넘긴다.
     */
    private fun TestScope.createManager() = RunningManagerImpl(
        gpsStatusClient = gpsStatusClient,
        tempRunDataSource = tempRunDataSource,
        externalScope = backgroundScope
    )

    private fun locationOf(lat: Double, lng: Double): Location = mockk<Location>().also {
        every { it.latitude } returns lat
        every { it.longitude } returns lng
    }

    // --- 초기 상태 ---

    @Test
    fun `초기 상태는 추적 중이 아니고 경로와 시간이 비어 있다`() = runTest {
        val manager = createManager()

        assertFalse(manager.isTracking.value)
        assertTrue(manager.pathPoints.value.isEmpty())
        assertEquals(0L, manager.durationInMillis.value)
    }

    // --- 추적 상태 제어 ---

    @Test
    fun `startResumeRun 호출 시 isTracking이 true가 된다`() = runTest {
        val manager = createManager()

        manager.isTracking.test {
            assertFalse(awaitItem())

            manager.startResumeRun()

            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `pauseRun 호출 시 isTracking이 false가 된다`() = runTest {
        val manager = createManager()
        manager.startResumeRun()

        manager.pauseRun()

        assertFalse(manager.isTracking.value)
    }

    @Test
    fun `stopRun은 추적을 멈추고 경로와 시간을 초기화한다`() = runTest {
        val manager = createManager()
        manager.startResumeRun()
        manager.addLocation(locationOf(37.0, 127.0))
        manager.updateDuration(5_000L)

        manager.stopRun()

        assertFalse(manager.isTracking.value)
        assertTrue(manager.pathPoints.value.isEmpty())
        assertEquals(0L, manager.durationInMillis.value)
    }

    @Test
    fun `stopRun은 임시 저장 데이터도 삭제한다`() = runTest {
        val manager = createManager()
        manager.persistState()

        manager.stopRun()

        assertEquals(1, tempRunDataSource.clearCallCount)
        assertNull(tempRunDataSource.savedState)
    }

    // --- 경로 누적 ---

    @Test
    fun `첫 addLocation은 폴리라인을 새로 만들어 좌표를 담는다`() = runTest {
        val manager = createManager()

        manager.addLocation(locationOf(37.5, 127.0))

        val pathPoints = manager.pathPoints.value
        assertEquals(1, pathPoints.size)
        assertEquals(1, pathPoints.first().size)
        assertEquals(37.5, pathPoints.first().first().latitude, 0.0001)
        assertEquals(127.0, pathPoints.first().first().longitude, 0.0001)
    }

    @Test
    fun `연속된 addLocation은 마지막 폴리라인에 누적된다`() = runTest {
        val manager = createManager()

        manager.addLocation(locationOf(37.0, 127.0))
        manager.addLocation(locationOf(37.1, 127.1))
        manager.addLocation(locationOf(37.2, 127.2))

        val pathPoints = manager.pathPoints.value
        assertEquals(1, pathPoints.size)
        assertEquals(3, pathPoints.first().size)
    }

    @Test
    fun `location이 null이면 경로가 변하지 않는다`() = runTest {
        val manager = createManager()
        manager.addLocation(locationOf(37.0, 127.0))
        val before = manager.pathPoints.value.first().size

        manager.addLocation(null)

        assertEquals(before, manager.pathPoints.value.first().size)
    }

    @Test
    fun `addEmptyPolyline 이후의 좌표는 새 폴리라인에 담긴다`() = runTest {
        val manager = createManager()
        manager.addLocation(locationOf(37.0, 127.0))

        manager.addEmptyPolyline()
        manager.addLocation(locationOf(37.5, 127.5))

        val pathPoints = manager.pathPoints.value
        assertEquals(2, pathPoints.size)
        assertEquals(1, pathPoints[0].size)
        assertEquals(1, pathPoints[1].size)
        assertEquals(37.5, pathPoints[1].first().latitude, 0.0001)
    }

    @Test
    fun `addLocation은 이전에 방출된 리스트를 변경하지 않고 새 리스트를 방출한다`() = runTest {
        val manager = createManager()

        manager.pathPoints.test {
            assertTrue(awaitItem().isEmpty())

            manager.addLocation(locationOf(37.0, 127.0))
            val first = awaitItem()
            assertEquals(1, first.first().size)

            manager.addLocation(locationOf(37.1, 127.1))
            val second = awaitItem()

            // 이전 방출본이 뒤늦게 오염되지 않아야 StateFlow 구독자가 변경을 감지할 수 있다
            assertEquals(1, first.first().size)
            assertEquals(2, second.first().size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // --- 시간 ---

    @Test
    fun `updateDuration은 전달받은 시간을 그대로 반영한다`() = runTest {
        val manager = createManager()

        manager.updateDuration(12_345L)

        assertEquals(12_345L, manager.durationInMillis.value)
    }

    // --- 임시 저장 / 복구 ---

    @Test
    fun `persistState는 현재 시간과 경로를 직렬화 가능한 형태로 저장한다`() = runTest {
        val manager = createManager()
        manager.updateDuration(60_000L)
        manager.addLocation(locationOf(37.0, 127.0))
        manager.addLocation(locationOf(37.1, 127.1))

        manager.persistState()

        val saved = tempRunDataSource.savedState
        assertEquals(1, tempRunDataSource.persistCallCount)
        assertEquals(60_000L, saved?.timeInMillis)
        assertEquals(
            listOf(
                listOf(
                    SerializableLatLng(37.0, 127.0),
                    SerializableLatLng(37.1, 127.1)
                )
            ),
            saved?.pathPoints
        )
    }

    @Test
    fun `restoreState는 저장된 시간과 경로를 그대로 되살린다`() = runTest {
        tempRunDataSource.savedState = TempRunState(
            timeInMillis = 90_000L,
            pathPoints = listOf(
                listOf(SerializableLatLng(37.0, 127.0), SerializableLatLng(37.1, 127.1)),
                listOf(SerializableLatLng(37.5, 127.5))
            )
        )
        val manager = createManager()

        manager.restoreState()

        assertEquals(90_000L, manager.durationInMillis.value)
        val pathPoints = manager.pathPoints.value
        assertEquals(2, pathPoints.size)
        assertEquals(2, pathPoints[0].size)
        assertEquals(1, pathPoints[1].size)
        assertEquals(37.1, pathPoints[0][1].latitude, 0.0001)
        assertEquals(127.5, pathPoints[1][0].longitude, 0.0001)
    }

    @Test
    fun `저장된 상태가 없으면 restoreState는 아무것도 바꾸지 않는다`() = runTest {
        val manager = createManager()
        manager.updateDuration(7_000L)

        manager.restoreState()

        assertEquals(7_000L, manager.durationInMillis.value)
        assertTrue(manager.pathPoints.value.isEmpty())
    }

    @Test
    fun `persistState 후 새 매니저에서 restoreState하면 상태가 이어진다`() = runTest {
        val original = createManager()
        original.updateDuration(30_000L)
        original.addLocation(locationOf(37.0, 127.0))
        original.persistState()

        val restored = createManager()
        restored.restoreState()

        assertEquals(30_000L, restored.durationInMillis.value)
        assertEquals(1, restored.pathPoints.value.size)
        assertEquals(37.0, restored.pathPoints.value.first().first().latitude, 0.0001)
    }

    // --- GPS ---

    @Test
    fun `isGpsEnabled의 초기값은 true다`() = runTest {
        val manager = createManager()

        assertTrue(manager.isGpsEnabled.value)
    }

    @Test
    fun `GPS가 꺼지면 isGpsEnabled가 false로 바뀐다`() = runTest {
        gpsStatusClient = FakeGpsStatusClient(initialEnabled = true)
        val manager = createManager()

        manager.isGpsEnabled.test {
            assertTrue(awaitItem())

            gpsStatusClient.emit(false)

            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `GPS가 다시 켜지면 isGpsEnabled가 true로 복구된다`() = runTest {
        gpsStatusClient = FakeGpsStatusClient(initialEnabled = false)
        val manager = createManager()

        manager.isGpsEnabled.test {
            // stateIn 의 initialValue 가 true 이므로 업스트림 값이 도착하기 전 true 가 먼저 보인다
            assertTrue(awaitItem())
            assertFalse(awaitItem())

            gpsStatusClient.emit(true)

            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
