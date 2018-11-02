package com.ober.arctic.util

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

interface AppExecutors {
    fun diskIO(): Executor
    fun networkIO(): Executor
    fun mainThread(): Executor
    fun miscellaneousThread(): Executor
}

class AppExecutorsImpl : AppExecutors {

    private val diskIO: Executor = Executors.newSingleThreadExecutor()
    private val networkIO: Executor = Executors.newFixedThreadPool(3)
    private val mainThread: Executor = MainThreadExecutor()
    private val miscellaneousThread: Executor = Executors.newSingleThreadExecutor()

    override fun diskIO(): Executor {
        return diskIO
    }

    override fun networkIO(): Executor {
        return networkIO
    }

    override fun mainThread(): Executor {
        return mainThread
    }

    override fun miscellaneousThread(): Executor {
        return miscellaneousThread
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}