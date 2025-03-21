package ro.aenigma.util

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

inline fun <reified T> String?.fromJson(): T? {
    return try {
        GsonBuilder()
            .setFieldNamingStrategy(CaseInsensitiveFieldNamingStrategy())
            .create()
            .fromJson(this, object : TypeToken<T>() {}.type)
    } catch (_: Exception) {
        null
    }
}

inline fun <reified T> T?.toJson(): String? {
    return try {
        GsonBuilder()
            .setFieldNamingStrategy(CapitalizedFieldNamingStrategy())
            .create()
            .toJson(this)
    } catch (_: Exception){
        null
    }
}
