package com.devhjs.runningtracker.data.mapper

import android.graphics.Bitmap
import com.devhjs.runningtracker.core.util.ImageUtils
import com.devhjs.runningtracker.data.local.RunEntity
import com.devhjs.runningtracker.domain.model.Run
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test

/**
 * [RunEntity] <-> [Run] 매핑 검증.
 *
 * 이미지 변환은 android.graphics.Bitmap 에 의존하므로 [ImageUtils]를 object mock 으로 대체한다.
 */
class RunMapperTest {

    @Before
    fun setUp() {
        mockkObject(ImageUtils)
    }

    @After
    fun tearDown() {
        unmockkObject(ImageUtils)
    }

    @Test
    fun `toDomain은 모든 필드를 그대로 옮긴다`() {
        val entity = RunEntity(
            timestamp = 1_700_000_000_000L,
            avgSpeedInKMH = 9.5f,
            distanceInMeters = 5_000,
            timeInMillis = 1_800_000L,
            caloriesBurned = 300
        ).also { it.id = 42 }

        val run = entity.toDomain()

        assertEquals(42, run.id)
        assertEquals(1_700_000_000_000L, run.timestamp)
        assertEquals(9.5f, run.avgSpeedInKMH, 0f)
        assertEquals(5_000, run.distanceInMeters)
        assertEquals(1_800_000L, run.timeInMillis)
        assertEquals(300, run.caloriesBurned)
    }

    @Test
    fun `toDomain에서 img가 null이면 변환을 시도하지 않고 null을 유지한다`() {
        val entity = RunEntity(img = null)

        val run = entity.toDomain()

        assertNull(run.img)
        verify(exactly = 0) { ImageUtils.bitmapToBytes(any()) }
    }

    @Test
    fun `toDomain에서 img가 있으면 ByteArray로 변환한다`() {
        val bitmap = mockk<Bitmap>()
        val bytes = byteArrayOf(1, 2, 3)
        every { ImageUtils.bitmapToBytes(bitmap) } returns bytes
        val entity = RunEntity(img = bitmap)

        val run = entity.toDomain()

        assertArrayEquals(bytes, run.img)
        verify(exactly = 1) { ImageUtils.bitmapToBytes(bitmap) }
    }

    @Test
    fun `toEntity는 모든 필드와 id를 그대로 옮긴다`() {
        val run = Run(
            id = 7,
            timestamp = 1_700_000_000_000L,
            avgSpeedInKMH = 11.2f,
            distanceInMeters = 10_000,
            timeInMillis = 3_600_000L,
            caloriesBurned = 600
        )

        val entity = run.toEntity()

        assertEquals(7, entity.id)
        assertEquals(1_700_000_000_000L, entity.timestamp)
        assertEquals(11.2f, entity.avgSpeedInKMH, 0f)
        assertEquals(10_000, entity.distanceInMeters)
        assertEquals(3_600_000L, entity.timeInMillis)
        assertEquals(600, entity.caloriesBurned)
    }

    @Test
    fun `toEntity에서 id가 null이면 Room이 자동 생성하도록 null로 남긴다`() {
        val run = Run(
            id = null,
            timestamp = 0L,
            avgSpeedInKMH = 0f,
            distanceInMeters = 0,
            timeInMillis = 0L,
            caloriesBurned = 0
        )

        val entity = run.toEntity()

        assertNull(entity.id)
    }

    @Test
    fun `toEntity에서 img가 있으면 Bitmap으로 변환한다`() {
        val bitmap = mockk<Bitmap>()
        val bytes = byteArrayOf(4, 5, 6)
        every { ImageUtils.bytesToBitmap(bytes) } returns bitmap
        val run = Run(
            timestamp = 0L,
            avgSpeedInKMH = 0f,
            distanceInMeters = 0,
            timeInMillis = 0L,
            caloriesBurned = 0,
            img = bytes
        )

        val entity = run.toEntity()

        assertSame(bitmap, entity.img)
        verify(exactly = 1) { ImageUtils.bytesToBitmap(bytes) }
    }

    @Test
    fun `Entity에서 Domain을 거쳐 다시 Entity로 왕복해도 값이 유지된다`() {
        val original = RunEntity(
            timestamp = 1_700_000_000_000L,
            avgSpeedInKMH = 8.3f,
            distanceInMeters = 3_200,
            timeInMillis = 1_200_000L,
            caloriesBurned = 192
        ).also { it.id = 3 }

        val roundTripped = original.toDomain().toEntity()

        assertEquals(original, roundTripped)
        assertEquals(original.id, roundTripped.id)
    }
}
