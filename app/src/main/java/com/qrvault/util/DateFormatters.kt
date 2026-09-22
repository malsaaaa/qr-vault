package com.qrvault.util

import java.text.DateFormat
import java.util.Date

object DateFormatters {

    fun format(timestamp: Long): String =
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
            .format(Date(timestamp))
}