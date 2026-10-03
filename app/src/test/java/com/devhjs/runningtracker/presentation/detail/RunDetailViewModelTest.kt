package com.devhjs.runningtracker.presentation.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.devhjs.runningtracker.domain.model.Run
import com.devhjs.runningtracker.domain.region.CountryProvider
import com.devhjs.runningtracker.domain.repository.MainRepository
import com.google.android.gms.maps.model.LatLng
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RunDetailViewModelTest {

    private val repository: MainRepository = mockk()

    private val run = Run(
        id = 1,
        timestamp = 1_700_000_000_000L,
        avgSpeedInKMH = 9f,
        distanceInMeters = 3_000,
        timeInMillis = 1_200_000L,
        caloriesBurned = 180
    )
    private val path = listOf(listOf(LatLng(37.5, 127.0), LatLng(37.501, 127.001)))

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(runId: Int = 1) = RunDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("runId" to runId)),
        mainRepository = repository,
        countryProvider = object : CountryProvider {
            override fun currentCountryCode() = "KR"
        }
    )

    @Test
    fun `진입하면 기록과 경로를 불러온다`() = runTest {
        coEvery { repository.getRunById(1) } returns run
        coEvery { repository.getRunPath(1) } returns path

        val state = createViewModel().state.value

        assertTrue(state.isLoaded)
        assertEquals(run, state.run)
        assertEquals(path, state.pathPoints)
    }

    @Test
    fun `기록이 없으면 경로를 조회하지 않고 빈 상태로 로딩을 끝낸다`() = runTest {
        coEvery { repository.getRunById(99) } returns null

        val state = createViewModel(runId = 99).state.value

        assertTrue(state.isLoaded)
        assertNull(state.run)
        coVerify(exactly = 0) { repository.getRunPath(any()) }
    }

    @Test
    fun `삭제 버튼을 누르면 확인 다이얼로그가 열리고 취소하면 닫힌다`() = runTest {
        coEvery { repository.getRunById(1) } returns run
        coEvery { repository.getRunPath(1) } returns emptyList()
        val viewModel = createViewModel()

        viewModel.onAction(RunDetailAction.OnDeleteClick)
        assertTrue(viewModel.state.value.showDeleteDialog)

        viewModel.onAction(RunDetailAction.OnDeleteDismiss)
        assertFalse(viewModel.state.value.showDeleteDialog)
        coVerify(exactly = 0) { repository.deleteRun(any()) }
    }

    @Test
    fun `삭제를 확인하면 기록을 지우고 이전 화면으로 돌아간다`() = runTest {
        coEvery { repository.getRunById(1) } returns run
        coEvery { repository.getRunPath(1) } returns path
        coEvery { repository.deleteRun(run) } just Runs
        val viewModel = createViewModel()

        viewModel.event.test {
            viewModel.onAction(RunDetailAction.OnDeleteClick)
            viewModel.onAction(RunDetailAction.OnDeleteConfirm)

            assertEquals(RunDetailEvent.NavigateUp, awaitItem())
        }
        coVerify(exactly = 1) { repository.deleteRun(run) }
        assertFalse(viewModel.state.value.showDeleteDialog)
    }

    @Test
    fun `쿠팡 카드를 누르면 링크를 연다`() = runTest {
        coEvery { repository.getRunById(1) } returns run
        coEvery { repository.getRunPath(1) } returns emptyList()
        val viewModel = createViewModel()

        viewModel.event.test {
            viewModel.onAction(RunDetailAction.OnCoupangClick("https://link"))

            assertEquals(RunDetailEvent.OpenUrl("https://link"), awaitItem())
        }
    }
}
