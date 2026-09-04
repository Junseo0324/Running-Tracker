package com.devhjs.runningtracker.data.repository

import app.cash.turbine.test
import com.devhjs.runningtracker.data.local.RunDAO
import com.devhjs.runningtracker.data.local.RunEntity
import com.devhjs.runningtracker.domain.model.Run
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * [MainRepositoryImpl]은 DAO 위임 + Entity/Domain 매핑만 담당하므로
 * DAO를 mock 으로 두고 "올바르게 위임했는가 / 올바르게 매핑했는가"를 검증한다.
 */
class MainRepositoryImplTest {

    private val runDao: RunDAO = mockk()
    private lateinit var repository: MainRepositoryImpl

    private val entity = RunEntity(
        timestamp = 1_700_000_000_000L,
        avgSpeedInKMH = 9.5f,
        distanceInMeters = 5_000,
        timeInMillis = 1_800_000L,
        caloriesBurned = 300
    ).also { it.id = 1 }

    private val run = Run(
        id = 1,
        timestamp = 1_700_000_000_000L,
        avgSpeedInKMH = 9.5f,
        distanceInMeters = 5_000,
        timeInMillis = 1_800_000L,
        caloriesBurned = 300
    )

    @Before
    fun setUp() {
        repository = MainRepositoryImpl(runDao)
    }

    @Test
    fun `insertRun은 Run을 Entity로 변환해 DAO에 위임한다`() = runTest {
        val captured = slot<RunEntity>()
        coEvery { runDao.insertRun(capture(captured)) } just Runs

        repository.insertRun(run)

        coVerify(exactly = 1) { runDao.insertRun(any()) }
        assertEquals(1, captured.captured.id)
        assertEquals(run.timestamp, captured.captured.timestamp)
        assertEquals(run.distanceInMeters, captured.captured.distanceInMeters)
    }

    @Test
    fun `deleteRun은 Run을 Entity로 변환해 DAO에 위임한다`() = runTest {
        val captured = slot<RunEntity>()
        coEvery { runDao.deleteRun(capture(captured)) } just Runs

        repository.deleteRun(run)

        coVerify(exactly = 1) { runDao.deleteRun(any()) }
        assertEquals(1, captured.captured.id)
    }

    @Test
    fun `getAllRunsSortedByDate는 Entity Flow를 Domain Flow로 매핑한다`() = runTest {
        every { runDao.getAllRunsSortedByDate() } returns flowOf(listOf(entity))

        repository.getAllRunsSortedByDate().test {
            assertEquals(listOf(run), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getAllRunsSortedByDistance는 Entity Flow를 Domain Flow로 매핑한다`() = runTest {
        every { runDao.getAllRunsSortedByDistance() } returns flowOf(listOf(entity))

        repository.getAllRunsSortedByDistance().test {
            assertEquals(listOf(run), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getAllRunsSortedByTimeInMillis는 Entity Flow를 Domain Flow로 매핑한다`() = runTest {
        every { runDao.getAllRunsSortedByTimeInMillis() } returns flowOf(listOf(entity))

        repository.getAllRunsSortedByTimeInMillis().test {
            assertEquals(listOf(run), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getAllRunsSortedByAvgSpeed는 Entity Flow를 Domain Flow로 매핑한다`() = runTest {
        every { runDao.getAllRunsSortedByAvgSpeed() } returns flowOf(listOf(entity))

        repository.getAllRunsSortedByAvgSpeed().test {
            assertEquals(listOf(run), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `getAllRunsSortedByCaloriesBurned는 Entity Flow를 Domain Flow로 매핑한다`() = runTest {
        every { runDao.getAllRunsSortedByCaloriesBurned() } returns flowOf(listOf(entity))

        repository.getAllRunsSortedByCaloriesBurned().test {
            assertEquals(listOf(run), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `기록이 없으면 빈 리스트를 그대로 방출한다`() = runTest {
        every { runDao.getAllRunsSortedByDate() } returns flowOf(emptyList())

        repository.getAllRunsSortedByDate().test {
            assertEquals(emptyList<Run>(), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `DAO Flow가 여러 번 방출하면 그때마다 매핑되어 전달된다`() = runTest {
        val second = RunEntity(timestamp = 2L).also { it.id = 2 }
        every { runDao.getAllRunsSortedByDate() } returns flowOf(
            listOf(entity),
            listOf(entity, second)
        )

        repository.getAllRunsSortedByDate().test {
            assertEquals(1, awaitItem().size)
            assertEquals(2, awaitItem().size)
            awaitComplete()
        }
    }

    @Test
    fun `합계 조회는 매핑 없이 DAO Flow를 그대로 전달한다`() = runTest {
        every { runDao.getTotalAvgSpeed() } returns flowOf(10.5f)
        every { runDao.getTotalDistance() } returns flowOf(12_345)
        every { runDao.getTotalCaloriesBurned() } returns flowOf(700)
        every { runDao.getTotalTimeInMillis() } returns flowOf(3_600_000L)

        repository.getTotalAvgSpeed().test {
            assertEquals(10.5f, awaitItem(), 0f)
            awaitComplete()
        }
        repository.getTotalDistance().test {
            assertEquals(12_345, awaitItem())
            awaitComplete()
        }
        repository.getTotalCaloriesBurned().test {
            assertEquals(700, awaitItem())
            awaitComplete()
        }
        repository.getTotalTimeInMillis().test {
            assertEquals(3_600_000L, awaitItem())
            awaitComplete()
        }
    }
}
