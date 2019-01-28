package com.jaiselrahman.wastatus.data.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.model.Video

@Database(entities = [Video::class], version = 1, exportSchema = false)
abstract class DB : RoomDatabase() {
    abstract fun videoDao(): VideoDao

    companion object {
        private val db = Room.databaseBuilder(
            App.getContext(),
            DB::class.java,
            App.getContext().getString(R.string.app_name)
        ).setJournalMode(JournalMode.TRUNCATE)
            .build()

        val videoDao = db.videoDao()
    }
}


