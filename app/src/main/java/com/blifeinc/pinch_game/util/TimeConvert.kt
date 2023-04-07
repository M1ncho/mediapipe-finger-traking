package com.blifeinc.pinch_game.util

import java.text.SimpleDateFormat
import java.util.*

class TimeConvert {

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    fun convertTime(time: Long): String {
        return simpleDateFormat.format(time)
    }

    fun convertDateTime(time: String): Date? {
        return simpleDateFormat.parse(time)
    }


}