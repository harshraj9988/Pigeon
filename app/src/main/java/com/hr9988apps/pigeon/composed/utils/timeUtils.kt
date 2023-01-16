package com.hr9988apps.pigeon.composed.utils

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

fun getTime(time: Long, format: String = "mm:ss") : String {
    return try{
        val date = Date(time)
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        formatter.format(date)
    }catch (_: Exception) {
        Log.e("TimeStringError", "Error converting time long to string", )
        format
    }
}
