package ro.aenigma.models.extensions

import ro.aenigma.data.database.EdgeEntity
import ro.aenigma.models.EdgeDto

object EdgeDtoExtensions {
    @JvmStatic
    fun EdgeDto.toEntity(): EdgeEntity {
        return EdgeEntity(
            sourceAddress = sourceAddress,
            targetAddress = targetAddress
        )
    }
}
