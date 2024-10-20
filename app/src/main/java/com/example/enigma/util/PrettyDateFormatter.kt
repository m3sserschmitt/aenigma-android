package com.example.enigma.util

import org.ocpsoft.prettytime.PrettyTime
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class PrettyDateFormatter
{
    companion object {

        @JvmStatic
        fun getTime(date: Date): String
        {
            return SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
        }

        @JvmStatic
        fun formatPastDate(date: Date): String {
            val currentDate = LocalDate.now()
            val inputDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

            return when {
                inputDate.isEqual(currentDate) -> "Today"
                inputDate.isEqual(currentDate.minusDays(1)) -> "Yesterday"
                inputDate.isAfter(currentDate.minusWeeks(1)) -> getDayOfWeek(inputDate)
                inputDate.isAfter(currentDate.minusMonths(1)) ->
                    "${getDayOfWeekShort(inputDate)}, ${getMonth(inputDate)} ${inputDate.dayOfMonth}${getDayOfMonthSuffix(inputDate)}"
                else ->
                    "${getDayOfWeekShort(inputDate)}, ${getMonth(inputDate)} ${inputDate.dayOfMonth}${getDayOfMonthSuffix(inputDate)} ${inputDate.year}"
            }
        }

        @JvmStatic
        fun getMonth(date: LocalDate): String
        {
            return date.month.name.lowercase().replaceFirstChar { char -> char.uppercase() }
        }

        @JvmStatic
        fun getDayOfWeek(date: LocalDate): String
        {
            return date.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
        }

        @JvmStatic
        fun getDayOfWeekShort(date: LocalDate): String
        {
            return getDayOfWeek(date).substring(0 until 2)
        }

        @JvmStatic
        fun getDayOfMonthSuffix(date: LocalDate): String {
            return when {
                date.dayOfMonth % 10 == 1 -> "st"
                date.dayOfMonth % 10 == 2 -> "nd"
                date.dayOfMonth % 10 == 3 -> "rd"
                else -> "th"
            }
        }

        @JvmStatic
        fun prettyTimeFormat(dateTimeOffset: String?): String?
        {
            if(dateTimeOffset == null)
            {
                return null
            }
            try {
                val date = OffsetDateTime.parse(dateTimeOffset)
                return PrettyTime().format(date.toLocalDateTime())
            } catch (_: Exception)
            {
                return null
            }
        }
    }
}
