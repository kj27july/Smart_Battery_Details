package com.smartbattery.details.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.smartbattery.details.ui.MainActivity
import java.lang.Exception

class BluebirdBatteryReceiver(private val instance: MainActivity) : BroadcastReceiver()  {
    override fun onReceive(context: Context?, intent: Intent?) {
        try {
            if (intent != null && Intent.ACTION_BATTERY_CHANGED == intent.action) {
                instance.handleBluebirdIntent(intent)
            }

        } catch (e: Exception) {
            Log.e("battery", e.printStackTrace().toString())
        }
    }
}