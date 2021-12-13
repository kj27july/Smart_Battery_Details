package com.smartbattery.details.ui

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.smartbattery.details.R
import com.smartbattery.details.receiver.BatteryReceiver

class MainActivity : AppCompatActivity() {
    private var batteryReceiver: BroadcastReceiver? = null

    var apiList = ArrayList<String>()
    var adapter: ArrayAdapter<*>? = null

    lateinit var health_percentage: String
    lateinit var battery_usage_numb: String
    lateinit var base_cumulative_charge: String
    lateinit var seconds_since_first_use: String
    lateinit var present_capacity: String
    lateinit var time_to_empty: String
    lateinit var time_to_full: String
    lateinit var present_charge: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val zebraApiList = findViewById<ListView>(R.id.zebra_api_list)
        adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, apiList)
        zebraApiList.adapter = adapter

        //  Battery info
        batteryReceiver = BatteryReceiver()
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = registerReceiver(batteryReceiver, filter)
//        if (batteryStatus != null) {
//            handleBatteryIntent(batteryStatus) //calling twice
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
    }

    private fun getBatteryType(intent: Intent): Int {
        return intent.extras!!.getInt("battery_type")
    }

    fun handleBatteryIntent(intent: Intent) {
        val mfg = intent.extras!!.getString("mfg")
        val partnumber = intent.extras!!.getString("partnumber")
        val serialnumber = intent.extras!!.getString("serialnumber")
        val ratedcapacity = intent.extras!!.getInt("ratedcapacity")
        val battery_decommission = intent.extras!!.getInt("battery_decommission")
        val total_cumulative_charge = intent.extras!!.getInt("total_cumulative_charge")
        if (getBatteryType(intent) == 202 || getBatteryType(intent) == 206) {
            battery_usage_numb = intent.extras!!.getInt("battery_usage_numb").toString()
        }

        if (getBatteryType(intent) == 201) {
            base_cumulative_charge = intent.extras!!.getInt("base_cumulative_charge").toString()
            seconds_since_first_use = intent.extras!!.getInt("seconds_since_first_use").toString()
            present_capacity = intent.extras!!.getInt("present_capacity").toString()
            health_percentage = intent.extras!!.getInt("health_percentage").toString()
            time_to_empty = intent.extras!!.getInt("time_to_empty").toString()
            time_to_full = intent.extras!!.getInt("time_to_full").toString()
            present_charge = intent.extras!!.getInt("present_charge").toString()
        }
        apiList.clear()

        apiList.add("=== STANDARD API ===")
        apiList.add("Manufacture Date: " + mfg!!)
        apiList.add("Serial Number: " + serialnumber!!)
        apiList.add("Health percentage: " + health_percentage)

        apiList.add("===NON STANDARD API ===")
        apiList.add("Part number: " + partnumber!!)
        apiList.add("Rated capacity: " + ratedcapacity)
        apiList.add("Battery decommission: " + battery_decommission)
        apiList.add("Total cumulative charge: " + total_cumulative_charge)
        apiList.add("Battery usage number: " + battery_usage_numb)
        apiList.add("Base cumulative charge: " + base_cumulative_charge)
        apiList.add("Seconds since first use: " + seconds_since_first_use)
        apiList.add("Present Capacity" + present_capacity)
        apiList.add("Time to empty charged: " + time_to_empty)
        apiList.add("Time to fully charged: " + time_to_full)
        apiList.add("charge remaining: " + present_charge)
    }
}