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
import com.smartbattery.details.receiver.PanasonicBatteryReceiver

class MainActivity : AppCompatActivity() {
    private var batteryReceiver: BroadcastReceiver? = null
    private var panasonicBatteryReceiver: BroadcastReceiver? = null

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
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, apiList)
        zebraApiList.adapter = adapter

        //Zebra  Battery info
        batteryReceiver = BatteryReceiver()
        val zebraFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        registerReceiver(batteryReceiver, zebraFilter)

        //Panasonic  Battery info
        val panasonicFilter = IntentFilter()
        panasonicFilter.addAction("com.panasonic.psn.batteryhealthcheck.api.RESPONSE ")
        registerReceiver(panasonicBatteryReceiver, panasonicFilter)

        // Send “Execute” broadcast Intent after prior preparation
        val intentExecute = Intent()
        intentExecute.action = "com.panasonic.psn.batteryhealthcheck.api.EXECUTE "
        sendBroadcast(intentExecute)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(batteryReceiver)
        unregisterReceiver(panasonicBatteryReceiver)
    }

    private fun getBatteryType(intent: Intent): Int {
        return intent.extras!!.getInt("battery_type")
    }

    fun handleBatteryIntent(intent: Intent) {
        val mfg = intent.extras!!.getString("mfg", "unknown")
        val partnumber = intent.extras!!.getString("partnumber", "unknown")
        val serialnumber = intent.extras!!.getString("serialnumber", "unknown")
        val ratedcapacity = intent.extras!!.getInt("ratedcapacity", -1)
        val battery_decommission = intent.extras!!.getInt("battery_decommission", -1)
        val total_cumulative_charge = intent.extras!!.getInt("total_cumulative_charge", -1)
        if (getBatteryType(intent) == 202 || getBatteryType(intent) == 206) {
            battery_usage_numb = intent.extras!!.getInt("battery_usage_numb", -1).toString()
        }

        if (getBatteryType(intent) == 201) {
            base_cumulative_charge = intent.extras!!.getInt("base_cumulative_charge", -1).toString()
            seconds_since_first_use =
                intent.extras!!.getInt("seconds_since_first_use", -1).toString()
            present_capacity = intent.extras!!.getInt("present_capacity", -1).toString()
            health_percentage = intent.extras!!.getInt("health_percentage", -1).toString()
            time_to_empty = intent.extras!!.getInt("time_to_empty", -1).toString()
            time_to_full = intent.extras!!.getInt("time_to_full", -1).toString()
            present_charge = intent.extras!!.getInt("present_charge", -1).toString()
        }
        apiList.clear()

        apiList.add("=== ZEBRA===")
        apiList.add("=== STANDARD API ===")
        apiList.add("Manufacture Date: " + mfg)
        apiList.add("Serial Number: " + serialnumber)
        apiList.add("Health percentage: " + health_percentage)

        apiList.add("===NON STANDARD API ===")
        apiList.add("Part number: " + partnumber)
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

    fun handlePanasonicIntent(intent: Intent) {
        val serial = intent.getStringExtra("serial")
        val date = intent.getStringExtra("product_date")
        val health = intent.getIntExtra("health", 0)
        val count = intent.getIntExtra("count", 0)

        //FZ-A3 Only from here
        val remaining = intent.getIntExtra("remaining", 0)
        val serial_2 = intent.getStringExtra("serial2")
        val date_2 = intent.getStringExtra("product_date2")
        val health_2 = intent.getIntExtra("health2", 0)
        val count_2 = intent.getIntExtra("count2", 0)
        val remaining_2 = intent.getIntExtra("remaining2", 0)

        apiList.add("=== PANASONIC ===")
        apiList.add("=== STANDARD API ===")
        apiList.add("Manufacture Date: " + date)
        apiList.add("Serial Number: " + serial)
        apiList.add("Health percentage: " + health)
        apiList.add("Manufacture Date2: " + date_2)
        apiList.add("Serial Number2: " + serial_2)
        apiList.add("Health percentage2: " + health_2)

        apiList.add("===NON STANDARD API ===")
        apiList.add("Cycle Count : " + count)
        apiList.add("Remaining Battery : " + remaining)
        apiList.add("Cycle Count2 : " + count_2)
        apiList.add("Remaining Battery2 : " + remaining_2)
    }
}