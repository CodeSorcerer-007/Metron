package com.metron.app

import android.app.Application
import com.metron.app.data.MetronRepository

class MetronApp : Application() {

    companion object {
        lateinit var instance: MetronApp
            private set

        val repository: MetronRepository by lazy {
            MetronRepository(instance)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
