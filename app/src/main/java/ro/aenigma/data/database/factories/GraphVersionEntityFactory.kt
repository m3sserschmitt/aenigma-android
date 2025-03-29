package ro.aenigma.data.database.factories

import ro.aenigma.data.database.GraphVersionEntity
import java.time.ZonedDateTime

class GraphVersionEntityFactory {
    companion object {
        @JvmStatic
        fun create(version: String): GraphVersionEntity {
            return GraphVersionEntity(version = version, dateCreated = ZonedDateTime.now())
        }
    }
}
