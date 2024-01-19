package com.example.enigma.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long): Date {
        return Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date): Long {
        return date.time
    }

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
}
