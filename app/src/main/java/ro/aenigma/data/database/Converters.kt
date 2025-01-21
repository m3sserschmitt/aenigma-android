package ro.aenigma.data.database

import androidx.room.TypeConverter
import ro.aenigma.util.MessageType
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.*

class Converters {

    @TypeConverter
    fun dateToString(date: Date): String
    {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())

        return dateFormat.format(date)
    }

    @TypeConverter
    fun stringToDate(date: String): Date?
    {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.getDefault())

        return dateFormat.parse(date)
    }

    @TypeConverter
    fun zonedDateTimeToString(date: ZonedDateTime): String
    {
        return date.toString()
    }

    @TypeConverter
    fun stringToZonedDateTime(date: String): ZonedDateTime?
    {
        return ZonedDateTime.parse(date)
    }

    @TypeConverter
    fun fromStatus(status: MessageType): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(value: String): MessageType {
        return MessageType.valueOf(value)
    }
}
