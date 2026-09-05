package com.devhjs.runningtracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RunEntity::class],
    version = 1
)
abstract class RunningDatabase : RoomDatabase() {

    abstract fun getRunDao(): RunDAO
}
