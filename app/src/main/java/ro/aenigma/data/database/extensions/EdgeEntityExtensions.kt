package ro.aenigma.data.database.extensions

import ro.aenigma.data.database.EdgeEntity
import ro.aenigma.models.EdgeDto

object EdgeEntityExtensions {
    @JvmStatic
    fun EdgeEntity.toDto(): EdgeDto {
        return EdgeDto(
            sourceAddress = sourceAddress,
            targetAddress = targetAddress
        )
    }
}
