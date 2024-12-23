package ro.aenigma.util

import com.google.gson.Gson

inline fun <reified T> copyBySerialization(data: T): T
{
    val gson = Gson()
    val serialized = gson.toJson(data)
    return gson.fromJson(serialized, T::class.java)
}