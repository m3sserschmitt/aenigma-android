package ro.aenigma.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.readValue
import retrofit2.converter.jackson.JacksonConverterFactory

object SerializerExtensions {
    @JvmStatic
    fun createJsonConverterFactory(): JacksonConverterFactory {
        return JacksonConverterFactory.create(createJsonMapper())
    }

    @JvmStatic
    fun createJsonMapper(): ObjectMapper {
        return JsonMapper.builder()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .propertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
            .findAndAddModules()
            .build()
    }

    @JvmStatic
    inline fun <reified T> String?.fromJson(): T? {
        return try {
            this?.let { createJsonMapper().readValue<T>(it) }
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    inline fun <reified T> T?.toJson(): String? {
        return try {
            createJsonMapper().writeValueAsString(this)
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    inline fun <reified T, reified V> T?.map(): V? {
        return if (this == null) {
            null
        } else try {
            val objectMapper = createJsonMapper()
            val json = objectMapper.writeValueAsString(this)
            objectMapper.readValue(json, V::class.java)
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    inline fun <reified T> T?.deepCopy(): T? {
        return this.map()
    }
}
