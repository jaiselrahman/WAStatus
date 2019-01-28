package com.jaiselrahman.wastatus.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class AppExecutors {
    companion object {
        val diskIO = Executors.newSingleThreadExecutor()!!
        val networkIO = Executors.newSingleThreadExecutor()!!
        val mainThread = object : Executor {
            private val handler = Handler(Looper.getMainLooper())
            override fun execute(command: Runnable?) {
                handler.post(command)
            }
        }
    }
}

operator fun Executor?.invoke(command: () -> Unit) {
    this?.execute(command)
}