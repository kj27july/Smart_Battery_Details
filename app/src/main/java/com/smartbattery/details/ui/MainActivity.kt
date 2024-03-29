package com.smartbattery.details.ui

import android.content.BroadcastReceiver
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.smartbattery.details.R
import com.smartbattery.details.receiver.ZebraBatteryReceiver
import com.smartbattery.details.receiver.PanasonicBatteryReceiver
import android.widget.TextView
import android.widget.TableLayout
import android.view.LayoutInflater
import android.widget.Toast
import com.honeywell.osservice.data.OSConstant
import com.honeywell.osservice.sdk.BatteryManager
import com.honeywell.osservice.sdk.CreateListener
import com.smartbattery.details.DetailsApp
import com.smartbattery.details.collectors.BluebirdBatteryCollector
import com.smartbattery.details.receiver.BluebirdBatteryReceiver
import com.smartbattery.details.utils.SmartBatteryUtil

class MainActivity : AppCompatActivity() {
    val TAG = "SmartBattery"
    private lateinit var stdApiTable: TableLayout
    private lateinit var nonStdApiTable: TableLayout
    private var mBatteryManager: BatteryManager? = null

    private var zebraStdMap = mutableMapOf<String, String>()
    private var zebraNonStdMap = mutableMapOf<String, String>()
    private var panasonicStdMap = mutableMapOf<String, String>()
    private var panasonicNonStdMap = mutableMapOf<String, String>()
    private var bluebirdStdMap = mutableMapOf<String, String>()
    private var bluebirdNonStdMap = mutableMapOf<String, String>()
    private var honeywellStdMap = mutableMapOf<String, String>()
    private var honeywellNonStdMap = mutableMapOf<String, String>()

    private var instance: MainActivity? = null
    private var zebraBatteryReceiver: BroadcastReceiver? = null
    private var panasonicBatteryReceiver: BroadcastReceiver? = null
    private var bluebirdBatteryReceiver: BroadcastReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        instance = this

        if (Build.MANUFACTURER.equals("panasonic", true)) {
            Log.d("kajal", "inPanasonic")
            panasonicBatteryReceiver = PanasonicBatteryReceiver(instance!!)
            val panasonicFilter = IntentFilter()
            panasonicFilter.addAction("com.panasonic.psn.batteryhealthcheck.api.RESPONSE")
            registerReceiver(panasonicBatteryReceiver, panasonicFilter)

            //  Send “Execute” broadcast Intent after prior preparation
            val intentExecute = Intent()
            intentExecute.action = "com.panasonic.psn.batteryhealthcheck.api.EXECUTE"
            sendBroadcast(intentExecute)
            Log.d("kajal", " Broadcast end")
        } else if (Build.MANUFACTURER.equals("Zebra Technologies", true)) {
            zebraBatteryReceiver = ZebraBatteryReceiver(instance!!)
            val zebraFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            registerReceiver(zebraBatteryReceiver, zebraFilter)
        } else if (Build.MANUFACTURER.equals("Bluebird", true)) {
            bluebirdBatteryReceiver = BluebirdBatteryReceiver(instance!!)
            val bluebirdFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            registerReceiver(bluebirdBatteryReceiver, bluebirdFilter)
        } else if (Build.MANUFACTURER.equals("Honeywell", true)) {
            Log.d(TAG, "initBatteryManager")
            BatteryManager.create(this, object : CreateListener<BatteryManager> {

                override fun onCreate(batteryManager: BatteryManager) {
                    Log.d(TAG, "onCreate-CreateListener: $batteryManager")
                    mBatteryManager = batteryManager
                    processHoneywell()
                }

                override fun onError(s: String) {
                    Log.e(TAG, "onError: BatteryManager:$s")
                }
            })
        } else
            Toast.makeText(this, "Device not supported", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.MANUFACTURER.equals(
                "Zebra Technologies",
                true
            ) && zebraBatteryReceiver != null
        ) unregisterReceiver(zebraBatteryReceiver)
        else if (Build.MANUFACTURER.equals(
                "panasonic",
                true
            ) && panasonicBatteryReceiver != null
        ) unregisterReceiver(panasonicBatteryReceiver)
        else if (Build.MANUFACTURER.equals(
                "Bluebird",
                true
            ) && bluebirdBatteryReceiver != null
        ) unregisterReceiver(bluebirdBatteryReceiver)
    }

    private fun getBatteryType(intent: Intent): Int {
        return intent.extras!!.getInt("battery_type")
    }

    fun handleZebraIntent(intent: Intent) {
        Log.d("kajal", intent.extras!!.getString("mfd", "unknown"))
        zebraNonStdMap["mfd"] = intent.extras!!.getString("mfd", "unknown")
        zebraNonStdMap["partnumber"] = intent.extras!!.getString("partnumber", "unknown")
        zebraStdMap["serialnumber"] = intent.extras!!.getString("serialnumber", "unknown")
        zebraNonStdMap["ratedcapacity"] = intent.extras!!.getInt("ratedcapacity").toString()
        zebraNonStdMap["battery_decommission"] =
            intent.extras!!.getInt("battery_decommission", -1).toString()
        zebraNonStdMap["total_cumulative_charge"] =
            intent.extras!!.getInt("total_cumulative_charge").toString()

        if (getBatteryType(intent) == 202 || getBatteryType(intent) == 206) {
            zebraNonStdMap["battery_usage_numb"] =
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

    fun handleBluebirdIntent(intent: Intent) {
        val batteryCollector = BluebirdBatteryCollector()
        bluebirdNonStdMap["is_smart_battery"] = batteryCollector.isSmartBattery().toString()
        bluebirdNonStdMap["unique_id"] = batteryCollector.getMfgBatteryId()
        bluebirdNonStdMap["manufacture_date"] = batteryCollector.getMfdDate()
        bluebirdNonStdMap["part_number"] = batteryCollector.getPartNo()
        bluebirdStdMap["serial_number"] = batteryCollector.getSerialNo()
        bluebirdNonStdMap["designed_capacity"] = batteryCollector.getRatedCapacity().toString()
        bluebirdNonStdMap["backup_voltage"] = batteryCollector.getBackupVoltage().toString()
        bluebirdNonStdMap["health_percentage"] = batteryCollector.getHealthPercentage().toString()
        bluebirdNonStdMap["full_capacity"] = batteryCollector.getCurrentCapacity().toString()
        bluebirdNonStdMap["capacity"] = batteryCollector.getCurrentCharge().toString()
        bluebirdNonStdMap["decommission_status"] =
            batteryCollector.getDecommissionStatus().toString()
        bluebirdNonStdMap["cycle_count"] = batteryCollector.getCycleCount().toString()
        bluebirdNonStdMap["cumulative_capacity"] =
            batteryCollector.getBaseCumulativeCharge().toString()
        bluebirdNonStdMap["first_used"] = batteryCollector.getFirstUsedDate()
        bluebirdNonStdMap["time_to_empty"] = batteryCollector.getTimeToEmpty().toString()
        bluebirdNonStdMap["time_to_full"] = batteryCollector.getTimeToFull().toString()

        stdApiTable = findViewById(R.id.standard_api_table)
        stdApiTable.removeAllViews()
        nonStdApiTable = findViewById(R.id.non_standard_api_table)
        nonStdApiTable.removeAllViews()

        val inflater = this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val stdTitleI = inflater.inflate(R.layout.table_title, null, false)
        stdTitleI.findViewById<TextView>(R.id.header_title).text = "STANDARD API"
        stdApiTable.addView(stdTitleI)

        for ((key, value) in bluebirdStdMap) {
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

        for ((key, value) in bluebirdNonStdMap) {
            val tableRowI = inflater.inflate(R.layout.table_row, null, false)
            val keyDI = tableRowI.findViewById<TextView>(R.id.key_data)
            val valueDI = tableRowI.findViewById<TextView>(R.id.value_data)
            keyDI.text = key
            valueDI.text = value
            nonStdApiTable.addView(tableRowI)
        }
    }

    fun handlePanasonicIntent(intent: Intent) {
        Log.d("kajal", "set intent")

        panasonicStdMap["serial"] = intent.getStringExtra("serial") ?: "unknown"
        panasonicNonStdMap["product_date"] = intent.getStringExtra("product_date") ?: "unknown"
        panasonicNonStdMap["health"] = intent.getIntExtra("health", 0).toString()
        panasonicNonStdMap["count"] = intent.getIntExtra("count", 0).toString()
        //FZ-A3 Only from here
        if (Build.MODEL.equals("FZ-A3", true) || Build.MODEL.equals("Toughbook FZ-A3", true)) {
            panasonicNonStdMap["remaining"] = intent.getIntExtra("remaining", 0).toString()
            panasonicStdMap["serial2"] = intent.getStringExtra("serial2") ?: "unknown"
            panasonicNonStdMap["product_date2"] =
                intent.getStringExtra("product_date2") ?: "unknown"
            panasonicNonStdMap["health2"] = intent.getIntExtra("health2", 0).toString()
            panasonicNonStdMap["count2"] = intent.getIntExtra("count2", 0).toString()
            panasonicNonStdMap["remaining2"] = intent.getIntExtra("remaining2", 0).toString()
        }

        stdApiTable = findViewById(R.id.standard_api_table)
        stdApiTable.removeAllViews()

        nonStdApiTable = findViewById(R.id.non_standard_api_table)
        nonStdApiTable.removeAllViews()

        val inflater = this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val stdTitleI = inflater.inflate(R.layout.table_title, null, false)
        stdTitleI.findViewById<TextView>(R.id.header_title).text = "STANDARD API"
        stdApiTable.addView(stdTitleI)

        for ((key, value) in panasonicStdMap) {
            Log.d("kajal", "update table")
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

    fun getString(constName: String): String {
        val data =
            mBatteryManager!!.getBatteryGaugeInfo(constName).getString(constName) ?: ""
        Log.d(TAG, "getString: $data")
        return data
    }

    fun processHoneywell() {
        honeywellStdMap["serial_number"] = getString(OSConstant.KEY_RESULT_BATTERY_SERIAL_NUMBER)
        honeywellNonStdMap["authentication"] =
            getString(OSConstant.KEY_RESULT_BATTERY_AUTHENTICATION)
        honeywellNonStdMap["voltage"] = getString(OSConstant.KEY_RESULT_BATTERY_VOLTAGE)
        honeywellNonStdMap["current"] = getString(OSConstant.KEY_RESULT_BATTERY_CURRENT)
        honeywellNonStdMap["temperature"] = getString(OSConstant.KEY_RESULT_BATTERY_TEMPERATURE)
        honeywellNonStdMap["battery_level"] =
            getString(OSConstant.KEY_RESULT_BATTERY_STATE_OF_CHARGE)
        honeywellNonStdMap["rated_capacity(SOTI Calculation)"] =
            SmartBatteryUtil.getRatedCapacity().toString()
        honeywellNonStdMap["current_capacity"] =
            getString(OSConstant.KEY_RESULT_BATTERY_FULL_CAPACITY)
        honeywellNonStdMap["current_charge"] =
            getString(OSConstant.KEY_RESULT_BATTERY_REMAINING_CAPACITY)
        honeywellNonStdMap["full_cap_compensated"] =
            getString(OSConstant.KEY_RESULT_BATTERY_FULL_CAPACITY_COMPENSATED)
        honeywellNonStdMap["full_cap_not_compensated"] =
            getString(OSConstant.KEY_RESULT_BATTERY_FULL_CAPACITY_NOT_COMPENSATED)
        honeywellNonStdMap["time_to_empty"] = getString(OSConstant.KEY_RESULT_BATTERY_TIME_TO_EMPTY)
        honeywellNonStdMap["time_to_full"] = getString(OSConstant.KEY_RESULT_BATTERY_TIME_TO_FULL)
        honeywellNonStdMap["manufacture_date"] =
            getString(OSConstant.KEY_RESULT_BATTERY_MANUFACTURING_DATE)
        honeywellNonStdMap["cycle_count"] = getString(OSConstant.KEY_RESULT_BATTERY_CYCLE_COUNT)
        honeywellNonStdMap["age_capacity"] = getString(OSConstant.KEY_RESULT_BATTERY_AGE_CAPACITY)
        honeywellNonStdMap["age_forecast"] = getString(OSConstant.KEY_RESULT_BATTERY_AGE_FORECAST)
        honeywellNonStdMap["age_time"] = getString(OSConstant.KEY_RESULT_BATTERY_AGE_TIME)
        honeywellNonStdMap["max_voltage"] = getString(OSConstant.KEY_RESULT_BATTERY_MAX_VOLTAGE)
        honeywellNonStdMap["min_voltage"] = getString(OSConstant.KEY_RESULT_BATTERY_MIN_VOLTAGE)
        honeywellNonStdMap["max_current"] = getString(OSConstant.KEY_RESULT_BATTERY_MAX_CURRENT)
        honeywellNonStdMap["min_current"] = getString(OSConstant.KEY_RESULT_BATTERY_MIN_CURRENT)
        honeywellNonStdMap["mix_temperature"] = getString(OSConstant.KEY_RESULT_BATTERY_MAX_TEMP)
        honeywellNonStdMap["min_temperature"] = getString(OSConstant.KEY_RESULT_BATTERY_MIN_TEMP)

        stdApiTable = findViewById(R.id.standard_api_table)
        stdApiTable.removeAllViews()
        nonStdApiTable = findViewById(R.id.non_standard_api_table)
        nonStdApiTable.removeAllViews()

        val inflater = this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val stdTitleI = inflater.inflate(R.layout.table_title, null, false)
        stdTitleI.findViewById<TextView>(R.id.header_title).text = "STANDARD API"
        stdApiTable.addView(stdTitleI)

        for ((key, value) in honeywellStdMap) {
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

        for ((key, value) in honeywellNonStdMap) {
            val tableRowI = inflater.inflate(R.layout.table_row, null, false)
            val keyDI = tableRowI.findViewById<TextView>(R.id.key_data)
            val valueDI = tableRowI.findViewById<TextView>(R.id.value_data)
            keyDI.text = key
            valueDI.text = value
            nonStdApiTable.addView(tableRowI)
        }
    }
}