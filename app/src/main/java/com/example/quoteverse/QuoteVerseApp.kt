package com.example.quoteverse

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QuoteVerseApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
