package com.smartbattery.details.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.smartbattery.details.ui.MainActivity
import java.lang.Exception

class PanasonicBatteryReceiver(private val instance: MainActivity) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("kajal", "onReceive: ")

        if (intent != null) {
            Log.d("kajal", "intent-onReceive: ")

            val serial = intent.getStringExtra("serial")
            val date = intent.getStringExtra("product_date")
            val health = intent.getIntExtra("health", 0)
            val count = intent.getIntExtra("count", 0)
            Log.d("kajal", "onReceive: $serial")
            Log.d("kajal", "onReceive: $date")
            Log.d("kajal", "onReceive: $health")
            Log.d("kajal", "onReceive: $count")
        }

        try {
            if (intent != null && intent.action == "com.panasonic.psn.batteryhealthcheck.api.RESPONSE") {
                instance.handlePanasonicIntent(intent)
            }
        } catch (e: Exception) {
            Log.e("battery", e.printStackTrace().toString())
        }
    }
}