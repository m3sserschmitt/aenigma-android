package ro.aenigma.util

import org.ocpsoft.prettytime.PrettyTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object PrettyDateFormatter {
    @JvmStatic
    fun messageCardStyleFormat(dateTime: ZonedDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return dateTime.withZoneSameInstant(ZoneId.systemDefault()).format(formatter)
    }

    @JvmStatic
    fun chatroomStyleFormat(dateTime: ZonedDateTime): String {
        val now = ZonedDateTime.now()
        val targetDateTime = dateTime.withZoneSameInstant(ZoneId.systemDefault())
        val daysAgo = ChronoUnit.DAYS.between(targetDateTime, now)
        val yearsAgo = ChronoUnit.YEARS.between(targetDateTime, now)

        return if (daysAgo < 1) {
            PrettyTime().formatUnrounded(targetDateTime)
        } else if (daysAgo < 7) {
            val formater = DateTimeFormatter.ofPattern("EEE")
            targetDateTime.format(formater)
        } else if(yearsAgo < 1) {
            val formater = DateTimeFormatter.ofPattern("EEE, d MMM")
            targetDateTime.format(formater)
        } else {
            val formater = DateTimeFormatter.ofPattern("EEE, d MMM yyyy")
            targetDateTime.format(formater)
        }
    }

    @JvmStatic
    fun format(dateTime: String?): String? {
        return try {
            val parsedDateTime =
                ZonedDateTime.parse(dateTime).withZoneSameInstant(ZoneId.systemDefault())
            format(parsedDateTime)
        } catch (_: Exception) {
            return null
        }
    }

    @JvmStatic
    fun format(dateTime: ZonedDateTime): String {
        return PrettyTime().format(dateTime)
    }
}
