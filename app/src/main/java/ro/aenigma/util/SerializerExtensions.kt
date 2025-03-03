package ro.aenigma.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

inline fun <reified T> String?.fromJson(): T? {
    return try {
        Gson().fromJson(this, object : TypeToken<T>() {}.type)
    } catch (_: Exception) {
        null
    }
}