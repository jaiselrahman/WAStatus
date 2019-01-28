package com.jaiselrahman.wastatus

import android.app.Application
import android.content.Context

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        app = this
    }

    companion object {
        private lateinit var app: App
        internal const val TAG = "WAStatus"

        fun getContext(): Context {
            return app.applicationContext
        }

    }
}