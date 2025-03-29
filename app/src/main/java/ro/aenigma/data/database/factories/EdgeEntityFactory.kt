package ro.aenigma.data.database.factories

import ro.aenigma.data.database.EdgeEntity

class EdgeEntityFactory {
    companion object {
        fun create(sourceAddress: String, targetAddress: String): EdgeEntity {
            return EdgeEntity(
                sourceAddress = sourceAddress,
                targetAddress = targetAddress
            )
        }
    }
}
