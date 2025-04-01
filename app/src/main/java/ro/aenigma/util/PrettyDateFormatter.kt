package ro.aenigma.util

import org.ocpsoft.prettytime.PrettyTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

object PrettyDateFormatter {
    @JvmStatic
    fun formatTime(date: ZonedDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        return date.withZoneSameInstant(ZoneId.systemDefault()).format(formatter)
    }

    @JvmStatic
    fun formatMessageDateTime(zonedDateTime: ZonedDateTime): String {
        val now = ZonedDateTime.now()
        val targetDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
        val daysAgo = ChronoUnit.DAYS.between(targetDateTime, now)
        val yearsAgo = ChronoUnit.YEARS.between(targetDateTime, now)

        return if (daysAgo < 2) {
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
    fun formatDateTime(zonedDateTime: String?): String? {
        return try {
            val parsedZonedDateTime =
                ZonedDateTime.parse(zonedDateTime).withZoneSameInstant(ZoneId.systemDefault())
            PrettyTime().format(parsedZonedDateTime)
        } catch (_: Exception) {
            return null
        }
    }
}
