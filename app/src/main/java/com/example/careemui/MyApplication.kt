package com.example.careemui

import android.app.Application
import com.google.android.libraries.places.api.Places

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Places.initialize(this, getString(R.string.googleID))
    }
}