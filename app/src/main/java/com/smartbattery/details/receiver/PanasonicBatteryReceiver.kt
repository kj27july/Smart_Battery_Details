package com.smartbattery.details.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.smartbattery.details.ui.MainActivity
import java.lang.Exception

class PanasonicBatteryReceiver(private val instance: MainActivity) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        try {
            if (intent != null && intent!!.action == "com.panasonic.psn.batteryhealthcheck.api.RESPONSE") {
                instance.handlePanasonicIntent(intent)
            }

        } catch (e: Exception) {
            Log.e("battery", e.printStackTrace().toString())
        }
    }
}