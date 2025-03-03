package ro.aenigma.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import ro.aenigma.models.GroupData
import ro.aenigma.models.MessageAction
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
    fun fromMessageAction(action: MessageAction): String {
        return Gson().toJson(action)
    }

    @TypeConverter
    fun toMessageAction(value: String): MessageAction {
        return Gson().fromJson(value, MessageAction::class.java)
    }

    @TypeConverter
    fun fromGroupData(value: GroupData): String
    {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toGroupData(value: String): GroupData
    {
        return Gson().fromJson(value, GroupData::class.java)
    }
}
