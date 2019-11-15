package com.jaiselrahman.wastatus.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.jaiselrahman.wastatus.App
import com.jaiselrahman.wastatus.R
import com.jaiselrahman.wastatus.data.db.DB
import org.koin.dsl.module

var dbModule = module {
    single {
        Room.databaseBuilder(
            App.getContext(),
            DB::class.java,
            App.getContext().getString(R.string.app_name)
        ).setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .build()
    }

    single { (get() as DB).videoDao() }
}