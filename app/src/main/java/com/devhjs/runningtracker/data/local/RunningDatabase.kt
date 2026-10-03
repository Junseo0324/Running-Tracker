package com.devhjs.runningtracker.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RunEntity::class, RunPathEntity::class],
    version = 2,
    autoMigrations = [
        // 1 -> 2: run_path 테이블 추가. 기존 running_table 은 건드리지 않는다.
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class RunningDatabase : RoomDatabase() {

    abstract fun getRunDao(): RunDAO
}
