package com.vibecoder

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class VibeCoderApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize any global singletons here
    }
}