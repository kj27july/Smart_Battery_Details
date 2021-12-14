package com.smartbattery.details.ui

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.smartbattery.details.R
import com.smartbattery.details.receiver.ZebraBatteryReceiver
import com.smartbattery.details.receiver.PanasonicBatteryReceiver

class MainActivity : AppCompatActivity() {
    private var instance: MainActivity? = null
    private var zebraBatteryReceiver: BroadcastReceiver? = null
    private var panasonicBatteryReceiver: BroadcastReceiver? = null

    var apiList = ArrayList<String>()
    var adapter: ArrayAdapter<*>? = null

    var health_percentage: String? = null
    var battery_usage_numb: String? = null
    var base_cumulative_charge: String? = null
    var seconds_since_first_use: String? = null
    var present_capacity: String? = null
    var time_to_empty: String? = null
    var time_to_full: String? = null
    var present_charge: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        instance = this

        val zebraApiList = findViewById<ListView>(R.id.api_list)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, apiList)
        zebraApiList.adapter = adapter

        //Zebra  Battery info
        if (Build.MANUFACTURER.equals("Zebra Technologies", true)) {
            zebraBatteryReceiver = ZebraBatteryReceiver(instance!!)
            val zebraFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus = registerReceiver(zebraBatteryReceiver, zebraFilter)
            if (batteryStatus != null) handleZebraIntent(batteryStatus)
        }

        //Panasonic  Battery info
        else if (Build.MANUFACTURER.equals("panasonic", true)) {
            panasonicBatteryReceiver = PanasonicBatteryReceiver(instance!!)
            val panasonicFilter = IntentFilter()
            panasonicFilter.addAction("com.panasonic.psn.batteryhealthcheck.api.RESPONSE")
            val panasonicBatteryStatus = registerReceiver(panasonicBatteryReceiver, panasonicFilter)
            if (panasonicBatteryStatus != null) handlePanasonicIntent(panasonicBatteryStatus)

            // Send “Execute” broadcast Intent after prior preparation
            val intentExecute = Intent()
            intentExecute.action = "com.panasonic.psn.batteryhealthcheck.api.EXECUTE"
            sendBroadcast(intentExecute)
        } else
            Toast.makeText(this, "Device not supported", Toast.LENGTH_LONG).show();
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(zebraBatteryReceiver)
        unregisterReceiver(panasonicBatteryReceiver)
    }

    private fun getBatteryType(intent: Intent): Int {
        return intent.extras!!.getInt("battery_type")
    }

    fun handleZebraIntent(intent: Intent) {
        val mfg = intent.extras!!.getString("mfg", "unknown")
        val partnumber = intent.extras!!.getString("partnumber", "unknown")
        val serialnumber = intent.extras!!.getString("serialnumber", "unknown")
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
        apiList.add("Serial Number: " + serialnumber)
        apiList.add("Battery usage number: " + battery_usage_numb)

        apiList.add("=== NON STANDARD API ===")
        apiList.add("Manufacture Date: " + mfg)
        apiList.add("Part number: " + partnumber)
        apiList.add("Rated capacity: " + ratedcapacity)
        apiList.add("Battery decommission: " + battery_decommission)
        apiList.add("Base cumulative charge: " + base_cumulative_charge)
        apiList.add("Seconds since first use: " + seconds_since_first_use)
        apiList.add("Total cumulative charge: " + total_cumulative_charge)
        apiList.add("Present Capacity:" + present_capacity)
        apiList.add("Time to empty charged: " + time_to_empty)
        apiList.add("Time to fully charged: " + time_to_full)
        apiList.add("Health percentage: " + health_percentage)
        apiList.add("Present_charge: " + present_charge)
        adapter?.notifyDataSetChanged()
    }

    fun handlePanasonicIntent(intent: Intent) {
        val serial = intent.getStringExtra("serial") ?: "unknown"
        val date = intent.getStringExtra("product_date") ?: "unknown"
        val health = intent.getIntExtra("health", 0)
        val count = intent.getIntExtra("count", 0)

        //FZ-A3 Only from here
        val remaining = intent.getIntExtra("remaining", 0)
        val serial_2 = intent.getStringExtra("serial2") ?: "unknown"
        val date_2 = intent.getStringExtra("product_date2") ?: "unknown"
        val health_2 = intent.getIntExtra("health2", 0)
        val count_2 = intent.getIntExtra("count2", 0)
        val remaining_2 = intent.getIntExtra("remaining2", 0)

        apiList.clear()
        apiList.add("=== STANDARD API ===")
        apiList.add("Serial Number: " + serial)
        apiList.add("Serial Number2: " + serial_2)
        apiList.add("Cycle Count : " + count)
        apiList.add("Cycle Count2 : " + count_2)

        apiList.add("=== NON STANDARD API ===")
        apiList.add("Manufacture Date: " + date)
        apiList.add("Manufacture Date2: " + date_2)
        apiList.add("Health percentage: " + health)
        apiList.add("Health percentage2: " + health_2)
        apiList.add("Remaining Battery : " + remaining)
        apiList.add("Remaining Battery2 : " + remaining_2)
        adapter?.notifyDataSetChanged()
    }
}