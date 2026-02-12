package ro.aenigma.data.database

import androidx.room.TypeConverter
import ro.aenigma.models.GroupDataDto
import ro.aenigma.util.StringExtensions.fromJson
import ro.aenigma.util.StringExtensions.toJson
import java.time.ZonedDateTime

class Converters {
    @TypeConverter
    fun zonedDateTimeToString(date: ZonedDateTime?): String? {
        return date.toString()
    }

    @TypeConverter
    fun stringToZonedDateTime(date: String?): ZonedDateTime? {
        return try {
            ZonedDateTime.parse(date)
        } catch (_: Exception) {
            ZonedDateTime.now()
        }
    }

    @TypeConverter
    fun fromGroupData(value: GroupDataDto?): String? {
        return value.toJson() ?: ""
    }

    @TypeConverter
    fun toGroupData(value: String?): GroupDataDto? {
        return value.fromJson() ?: GroupDataDto()
    }

    @TypeConverter
    fun toListOfStrings(value: String?): List<String>? {
        return value.fromJson() ?: listOf()
    }

    @TypeConverter
    fun fromListOfStrings(value: List<String>?): String? {
        return value.toJson() ?: ""
    }
}
