package ro.aenigma.util

import org.ocpsoft.prettytime.PrettyTime
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class PrettyDateFormatter
{
    companion object {

        @JvmStatic
        fun getTime(date: ZonedDateTime): String
        {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            return date.withZoneSameInstant(ZoneId.systemDefault()).format(formatter)
        }

        @JvmStatic
        fun formatPastDate(date: ZonedDateTime): String {
            val currentDate = ZonedDateTime.now().toLocalDate()
            val inputDate = date.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()

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
            return try {
                PrettyTime().format(ZonedDateTime.parse(dateTimeOffset))
            }
            catch (_: Exception)
            {
                return null
            }
        }
    }
}
