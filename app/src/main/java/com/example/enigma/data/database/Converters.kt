package com.example.enigma.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.util.*

class Converters {

    @TypeConverter
    fun pathToString(path: List<String>): String
    {
        val gson = Gson()
        return gson.toJson(path)
    }

    @TypeConverter
    fun pathFromString(stringPath: String): List<String>?
    {
        val gson = Gson()
        val itemType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(stringPath, itemType)
    }

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
}
