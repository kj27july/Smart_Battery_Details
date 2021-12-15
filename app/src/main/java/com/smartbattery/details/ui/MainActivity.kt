package com.smartbattery.details.ui

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.smartbattery.details.R
import com.smartbattery.details.receiver.ZebraBatteryReceiver
import com.smartbattery.details.receiver.PanasonicBatteryReceiver
import android.widget.TextView

import android.widget.TableLayout

import android.view.LayoutInflater
import android.widget.Toast


class MainActivity : AppCompatActivity() {
    private lateinit var stdApiTable: TableLayout
    private lateinit var nonStdApiTable: TableLayout

    private var zebraStdMap = mutableMapOf<String, String>()
    private var zebraNonStdMap = mutableMapOf<String, String>()
    private var panasonicStdMap = mutableMapOf<String, String>()
    private var panasonicNonStdMap = mutableMapOf<String, String>()

    private var instance: MainActivity? = null
    private var zebraBatteryReceiver: BroadcastReceiver? = null
    private var panasonicBatteryReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        instance = this

        //Zebra  Battery info
        if (Build.MANUFACTURER.equals("Zebra Technologies", true)) {
            zebraBatteryReceiver = ZebraBatteryReceiver(instance!!)
            val zebraFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            registerReceiver(zebraBatteryReceiver, zebraFilter)
        }
        //Panasonic  Battery info
        else if (Build.MANUFACTURER.equals("panasonic", true)) {
            panasonicBatteryReceiver = PanasonicBatteryReceiver(instance!!)
            val panasonicFilter = IntentFilter()
            panasonicFilter.addAction("com.panasonic.psn.batteryhealthcheck.api.RESPONSE")
            registerReceiver(panasonicBatteryReceiver, panasonicFilter)

            //  Send “Execute” broadcast Intent after prior preparation
            val intentExecute = Intent()
            intentExecute.action = "com.panasonic.psn.batteryhealthcheck.api.EXECUTE"
            sendBroadcast(intentExecute)
        } else
            Toast.makeText(this, "Device not supported", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
//        unregisterReceiver(zebraBatteryReceiver)
//        unregisterReceiver(panasonicBatteryReceiver)
    }

    private fun getBatteryType(intent: Intent): Int {
        return intent.extras!!.getInt("battery_type")
    }

    fun handleZebraIntent(intent: Intent) {
        zebraNonStdMap["mfd"] = intent.extras!!.getString("mfd", "unknown")
        zebraNonStdMap["partnumber"] = intent.extras!!.getString("partnumber", "unknown")
        zebraStdMap["serialnumber"] = intent.extras!!.getString("serialnumber", "unknown")
        zebraNonStdMap["ratedcapacity"] = intent.extras!!.getInt("ratedcapacity").toString()
        zebraNonStdMap["battery_decommission"] =
            intent.extras!!.getInt("battery_decommission",-1).toString()
        zebraNonStdMap["total_cumulative_charge"] =
            intent.extras!!.getInt("total_cumulative_charge").toString()

        if (getBatteryType(intent) == 202 || getBatteryType(intent) == 206) {
            zebraStdMap["battery_usage_numb"] =
                intent.extras!!.getInt("battery_usage_numb").toString()
        }

        if (getBatteryType(intent) == 201) {
            zebraNonStdMap["base_cumulative_charge"] =
                intent.extras!!.getInt("base_cumulative_charge").toString()
            zebraNonStdMap["seconds_since_first_use"] =
                intent.extras!!.getInt("seconds_since_first_use").toString()
            zebraNonStdMap["present_capacity"] =
                intent.extras!!.getInt("present_capacity").toString()
            zebraNonStdMap["health_percentage"] =
                intent.extras!!.getInt("health_percentage").toString()
            zebraNonStdMap["time_to_empty"] = intent.extras!!.getInt("time_to_empty").toString()
            zebraNonStdMap["time_to_full"] = intent.extras!!.getInt("time_to_full").toString()
            zebraNonStdMap["present_charge"] =
                intent.extras!!.getInt("present_charge").toString()
        }

        stdApiTable = findViewById(R.id.standard_api_table)
        stdApiTable.removeAllViews()
        nonStdApiTable = findViewById(R.id.non_standard_api_table)
        nonStdApiTable.removeAllViews()

        val inflater = this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val stdTitleI = inflater.inflate(R.layout.table_title, null, false)
        stdTitleI.findViewById<TextView>(R.id.header_title).text = "STANDARD API"
        stdApiTable.addView(stdTitleI)

        for ((key, value) in zebraStdMap) {
            val tableRowI = inflater.inflate(R.layout.table_row, null, false)
            val keyDI = tableRowI.findViewById<TextView>(R.id.key_data)
            val valueDI = tableRowI.findViewById<TextView>(R.id.value_data)
            keyDI.text = key
            valueDI.text = value
            stdApiTable.addView(tableRowI)
        }

        val nonStdTitleI = inflater.inflate(R.layout.table_title, null, false)
        nonStdTitleI.findViewById<TextView>(R.id.header_title).text = "NON STANDARD API"
        nonStdApiTable.addView(nonStdTitleI)

        for ((key, value) in zebraNonStdMap) {
            val tableRowI = inflater.inflate(R.layout.table_row, null, false)
            val keyDI = tableRowI.findViewById<TextView>(R.id.key_data)
            val valueDI = tableRowI.findViewById<TextView>(R.id.value_data)
            keyDI.text = key
            valueDI.text = value
            nonStdApiTable.addView(tableRowI)
        }
    }

    fun handlePanasonicIntent(intent: Intent) {
        panasonicStdMap["serial"] = intent.getStringExtra("serial") ?: "unknown"
        panasonicNonStdMap["product_date"] = intent.getStringExtra("product_date") ?: "unknown"
        panasonicNonStdMap["health"] = intent.getIntExtra("health", 0).toString()
        panasonicStdMap["count"] = intent.getIntExtra("count", 0).toString()
        //FZ-A3 Only from here
        panasonicNonStdMap["remaining"] = intent.getIntExtra("remaining", 0).toString()
        panasonicStdMap["serial2"] = intent.getStringExtra("serial2") ?: "unknown"
        panasonicNonStdMap["product_date2"] = intent.getStringExtra("product_date2") ?: "unknown"
        panasonicNonStdMap["health2"] = intent.getIntExtra("health2", 0).toString()
        panasonicStdMap["count2"] = intent.getIntExtra("count2", 0).toString()
        panasonicNonStdMap["remaining2"] = intent.getIntExtra("remaining2", 0).toString()

        stdApiTable = findViewById(R.id.standard_api_table)
        stdApiTable.removeAllViews()

        nonStdApiTable = findViewById(R.id.non_standard_api_table)
        nonStdApiTable.removeAllViews()

        val inflater = this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val stdTitleI = inflater.inflate(R.layout.table_title, null, false)
        stdTitleI.findViewById<TextView>(R.id.header_title).text = "STANDARD API"
        stdApiTable.addView(stdTitleI)

        for ((key, value) in panasonicStdMap) {
            val tableRowI = inflater.inflate(R.layout.table_row, null, false)
            val keyDI = tableRowI.findViewById<TextView>(R.id.key_data)
            val valueDI = tableRowI.findViewById<TextView>(R.id.value_data)
            keyDI.text = key
            valueDI.text = value
            stdApiTable.addView(tableRowI)
        }

        val nonStdTitleI = inflater.inflate(R.layout.table_title, null, false)
        nonStdTitleI.findViewById<TextView>(R.id.header_title).text = "NON STANDARD API"
        nonStdApiTable.addView(nonStdTitleI)

        for ((key, value) in panasonicNonStdMap) {
            val tableRowI = inflater.inflate(R.layout.table_row, null, false)
            val keyDI = tableRowI.findViewById<TextView>(R.id.key_data)
            val valueDI = tableRowI.findViewById<TextView>(R.id.value_data)
            keyDI.text = key
            valueDI.text = value
            nonStdApiTable.addView(tableRowI)
        }
    }
}