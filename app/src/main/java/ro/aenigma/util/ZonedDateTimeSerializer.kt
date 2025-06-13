package ro.aenigma.util

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.time.ZonedDateTime

class ZonedDateTimeSerializer : JsonSerializer<ZonedDateTime?>() {
    override fun serialize(
        value: ZonedDateTime?,
        gen: JsonGenerator,
        serializers: SerializerProvider
    ) {
        gen.writeString(value.toString())
    }
}
