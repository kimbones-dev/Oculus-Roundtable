package com.example

import android.app.Application
import com.example.data.local.OculusDatabase

class OculusApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Pre-warm Room database instance
        OculusDatabase.getInstance(this)
    }
}
