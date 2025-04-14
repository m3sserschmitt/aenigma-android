package ro.aenigma.data.database.factories

import ro.aenigma.data.database.EdgeEntity

object EdgeEntityFactory {
    @JvmStatic
    fun create(sourceAddress: String, targetAddress: String): EdgeEntity {
        return EdgeEntity(
            sourceAddress = sourceAddress,
            targetAddress = targetAddress
        )
    }
}
