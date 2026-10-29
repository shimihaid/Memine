package com.example.lasttime.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lasttime.data.local.dao.ActivityDao
import com.example.lasttime.data.local.entity.ActivityEntity
import com.example.lasttime.data.local.entity.ActivityHistoryEntity

@Database(
    entities = [ActivityEntity::class, ActivityHistoryEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LastTimeDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
}
