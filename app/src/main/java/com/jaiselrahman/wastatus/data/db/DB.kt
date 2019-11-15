package com.jaiselrahman.wastatus.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jaiselrahman.wastatus.model.Video

@Database(entities = [Video::class], version = 1, exportSchema = false)
abstract class DB : RoomDatabase() {
    abstract fun videoDao(): VideoDao
}


