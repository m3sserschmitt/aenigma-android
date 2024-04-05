package com.example.enigma.util

import org.ocpsoft.prettytime.PrettyTime
import java.util.Date
import java.util.Locale

fun prettyDateFormatting(date: Date): String
{
    val prettyTime = PrettyTime(Locale.getDefault())
    return prettyTime.format(date)
}
