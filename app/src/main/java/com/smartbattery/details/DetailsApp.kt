package com.smartbattery.details

import android.app.Application

class DetailsApp: Application() {
    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}