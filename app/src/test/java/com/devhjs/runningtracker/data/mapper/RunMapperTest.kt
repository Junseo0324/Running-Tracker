package com.devhjs.runningtracker.data.mapper

import com.devhjs.runningtracker.data.local.RunEntity
import com.devhjs.runningtracker.domain.model.Run
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * [RunEntity] <-> [Run] 매핑 검증.
 *
 * 이미지는 양쪽 모두 PNG 바이트(ByteArray)로 다루므로 매핑 과정에서 어떤 변환도 일어나지 않는다.
 * 과거에는 Entity 가 Bitmap 을 들고 있어 매 조회마다 PNG 디코딩 -> 재압축 왕복이 발생했고,
 * 이것이 저장된 기록 수에 비례해 메모리를 소모하는 원인이었다.
 * 아래 테스트들은 그 왕복이 다시 들어오는 것을 막는 회귀 방지 장치다.
 */
class RunMapperTest {

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
    fun `toDomain에서 img가 null이면 null을 유지한다`() {
        val entity = RunEntity(img = null)

        val run = entity.toDomain()

        assertNull(run.img)
    }

    @Test
    fun `toDomain은 img 바이트를 재인코딩 없이 동일 인스턴스로 전달한다`() {
        val bytes = byteArrayOf(1, 2, 3)
        val entity = RunEntity(img = bytes)

        val run = entity.toDomain()

        // 같은 인스턴스여야 한다. 복사본이라면 어딘가에서 디코딩/재압축이 일어난 것이다.
        assertSame(bytes, run.img)
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
    fun `toEntity는 img 바이트를 재인코딩 없이 동일 인스턴스로 전달한다`() {
        val bytes = byteArrayOf(4, 5, 6)
        val run = Run(
            timestamp = 0L,
            avgSpeedInKMH = 0f,
            distanceInMeters = 0,
            timeInMillis = 0L,
            caloriesBurned = 0,
            img = bytes
        )

        val entity = run.toEntity()

        assertSame(bytes, entity.img)
    }

    @Test
    fun `Entity에서 Domain을 거쳐 다시 Entity로 왕복해도 이미지 바이트가 보존된다`() {
        val bytes = byteArrayOf(9, 8, 7, 6, 5)
        val original = RunEntity(
            timestamp = 1_700_000_000_000L,
            avgSpeedInKMH = 8.3f,
            distanceInMeters = 3_200,
            timeInMillis = 1_200_000L,
            caloriesBurned = 192,
            img = bytes
        ).also { it.id = 3 }

        val roundTripped = original.toDomain().toEntity()

        assertEquals(original.timestamp, roundTripped.timestamp)
        assertEquals(original.avgSpeedInKMH, roundTripped.avgSpeedInKMH, 0f)
        assertEquals(original.distanceInMeters, roundTripped.distanceInMeters)
        assertEquals(original.timeInMillis, roundTripped.timeInMillis)
        assertEquals(original.caloriesBurned, roundTripped.caloriesBurned)
        assertEquals(original.id, roundTripped.id)
        assertArrayEquals(bytes, roundTripped.img)
        assertSame(bytes, roundTripped.img)
    }
}
