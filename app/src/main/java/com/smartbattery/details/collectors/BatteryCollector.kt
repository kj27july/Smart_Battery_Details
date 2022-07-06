package com.smartbattery.details.collectors

import android.content.Intent
import android.content.IntentFilter
import com.smartbattery.details.DetailsApp
import com.smartbattery.details.ui.MainActivity

class BatteryCollector(private val uniqueId: String? = null) {
    private var intent = register()
    //private var initParams = getConsumerInit()

    companion object {
        const val IS_SMART_BATTERY = "is_smart_battery"
        const val MANUFACTURE_DATE = "manufacture_date"
        const val PART_NO = "part_number"
        const val SERIAL_NUMBER = "serial_number"
        const val RATED_CAPACITY = "designed_capacity"
        const val BACKUP_VOLTAGE = "backup_voltage"
        const val HEALTH_PERCENTAGE = "health_percentage"
        const val CURRENT_CAPACITY = "full_capacity"
        const val CURRENT_CHARGE = "capacity"
        const val DECOMMISSION_STATUS = "decommission_status"
        const val CYCLE_COUNT = "cycle_count"
        const val BASE_CUMULATIVE_CHARGE = "cumulative_capacity"
        const val FIRST_USED_DATE = "first_used"
        const val TIME_TO_EMPTY = "time_to_empty"
        const val TIME_TO_FULL = "time_to_full"

        const val EMPTY_STRING = ""
        const val ONE_MILLISECOND = 1000
    }

    private fun register(): Intent? {
        val bluebirdFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        return DetailsApp.instance.registerReceiver(null, bluebirdFilter)
    }

    /*private fun getConsumerInit(): JSONObject {
        return if (uniqueId != null) {
            val initInfo = BluebirdDB.getInstance().initInfoDAO().get(uniqueId)
            if (initInfo != null) JSONObject(initInfo.parameters) else JSONObject()
        } else JSONObject()
    }*/

    private fun getString(property: String, defaultValue: String): String {
        return intent?.extras?.getString(property, defaultValue) ?: defaultValue
    }

    private fun getInt(property: String, defaultValue: Int): Int {
        return intent?.extras?.getInt(property, defaultValue) ?: defaultValue
    }

    fun isSmartBattery(): Boolean {
        return intent?.extras?.getBoolean(IS_SMART_BATTERY, false) ?: false
    }

    fun getMfgBatteryId(): String = getSerialNo()

    fun getMfdDate(): String = getString(MANUFACTURE_DATE, EMPTY_STRING)

    fun getPartNo(): String = getString(PART_NO, EMPTY_STRING)

    fun getSerialNo(): String = getString(SERIAL_NUMBER, EMPTY_STRING)

    fun getRatedCapacity(): Int = getInt(RATED_CAPACITY, -1)

    fun getBackupVoltage(): Int = getInt(BACKUP_VOLTAGE, 0)

    fun getHealthPercentage(): Int = getInt(HEALTH_PERCENTAGE, -1)

    fun getCurrentCapacity(): Int = getInt(CURRENT_CAPACITY, -1)

    fun getCurrentCharge(): Int = getInt(CURRENT_CHARGE, -1)

    fun getDecommissionStatus(): Int = getInt(DECOMMISSION_STATUS, -1)

    fun getCycleCount(): Double = intent?.extras?.getDouble(CYCLE_COUNT, -1.0) ?: -1.0

    fun getBaseCumulativeCharge(): Int = getInt(BASE_CUMULATIVE_CHARGE, -1)

    fun getFirstUsedDate(): String = getString(FIRST_USED_DATE, EMPTY_STRING)

    fun getTimeToEmpty(): Long {
        val timeToEmpty = intent?.extras?.getLong(TIME_TO_EMPTY, -1L) ?: -1L
        return if (timeToEmpty == -1L) timeToEmpty else timeToEmpty / ONE_MILLISECOND
    }

    fun getTimeToFull(): Long {
        val timeToFull = intent?.extras?.getLong(TIME_TO_FULL, -1L) ?: -1L
        return if (timeToFull == -1L) timeToFull else timeToFull / ONE_MILLISECOND
    }

}