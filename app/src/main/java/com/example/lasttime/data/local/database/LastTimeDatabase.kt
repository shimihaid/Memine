package com.example.lasttime.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lasttime.data.local.dao.ActivityDao
import com.example.lasttime.data.local.dao.CategoryDao
import com.example.lasttime.data.local.entity.ActivityEntity
import com.example.lasttime.data.local.entity.ActivityHistoryEntity
import com.example.lasttime.data.local.entity.CategoryEntity

@Database(
    entities = [ActivityEntity::class, ActivityHistoryEntity::class, CategoryEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class LastTimeDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun categoryDao(): CategoryDao
}
