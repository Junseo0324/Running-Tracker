package com.devhjs.runningtracker.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * 내보낸 스키마(app/schemas)를 기준으로 실제 SQLite 에서 마이그레이션을 실행해
 * 업데이트한 사용자의 기존 기록이 그대로 남는지 검증한다.
 */
@RunWith(AndroidJUnit4::class)
class RunningDatabaseMigrationTest {

    private val testDb = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        RunningDatabase::class.java
    )

    @Test
    fun migrate1To2_기존_기록이_보존되고_경로_테이블이_추가된다() {
        val img = byteArrayOf(1, 2, 3, 4)
        helper.createDatabase(testDb, 1).apply {
            execSQL(
                "INSERT INTO running_table (id, timestamp, avgSpeedInKMH, distanceInMeters, timeInMillis, caloriesBurned, img) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)",
                arrayOf(7, 1_700_000_000_000L, 9.5f, 5_000, 1_800_000L, 300, img)
            )
            close()
        }

        // validateDroppedTables = true: 스키마가 2.json 과 정확히 일치하는지까지 검사한다.
        val db = helper.runMigrationsAndValidate(testDb, 2, true)

        db.query("SELECT timestamp, distanceInMeters, timeInMillis, caloriesBurned, img FROM running_table WHERE id = 7").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(1_700_000_000_000L, c.getLong(0))
            assertEquals(5_000, c.getInt(1))
            assertEquals(1_800_000L, c.getLong(2))
            assertEquals(300, c.getInt(3))
            assertArrayEquals(img, c.getBlob(4))
        }
        db.query("SELECT COUNT(*) FROM run_path").use { c ->
            assertTrue(c.moveToFirst())
            assertEquals(0, c.getInt(0))
        }
        db.close()
    }
}
