package com.smartbattery.details.utils

import android.annotation.SuppressLint
import android.content.Context
import com.smartbattery.details.DetailsApp
import java.text.SimpleDateFormat
import java.util.*

object SmartBatteryUtil {
    private const val POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile"
    private const val BATTERY_CAPACITY_METHOD = "getBatteryCapacity"

    @SuppressLint("PrivateApi")
    fun getRatedCapacity(): Int {
        var batteryCapacity = 0.0
        try {
            val powerProfile = Class.forName(POWER_PROFILE_CLASS)
                .getConstructor(Context::class.java)
                .newInstance(DetailsApp.instance)
            batteryCapacity = Class.forName(POWER_PROFILE_CLASS)
                .getMethod(BATTERY_CAPACITY_METHOD)
                .invoke(powerProfile) as Double
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return batteryCapacity.toInt()
    }

    fun formatMfdDate(manufactureDate: String): String {
        if (manufactureDate == "") return manufactureDate
        val currentDateFormat = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
        val requiredDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return requiredDateFormat.format(currentDateFormat.parse(manufactureDate)!!)
    }
}