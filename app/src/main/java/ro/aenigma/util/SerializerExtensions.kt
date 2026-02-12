package ro.aenigma.util

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import retrofit2.converter.jackson.JacksonConverterFactory
import ro.aenigma.util.StringExtensions.canonicalize
import ro.aenigma.util.StringExtensions.toJson
import java.time.ZonedDateTime

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
            .defaultPropertyInclusion(
                JsonInclude.Value.construct(
                    JsonInclude.Include.NON_NULL,
                    JsonInclude.Include.ALWAYS
                )
            )
            .propertyNamingStrategy(PropertyNamingStrategies.UPPER_CAMEL_CASE)
            .addModule(JavaTimeModule())
            .addModule(SimpleModule().apply {
                addSerializer(ZonedDateTime::class.java, ZonedDateTimeSerializer())
                addDeserializer(ZonedDateTime::class.java, ZonedDateTimeDeserializer())
            })
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .findAndAddModules()
            .build()
    }

    @JvmStatic
    inline fun <reified T> T?.toCanonicalJson(): String? {
        return try {
            this.toJson().canonicalize()
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
