package ro.aenigma.data.database.factories

import ro.aenigma.data.database.GraphVersionEntity
import java.time.ZonedDateTime

object GraphVersionEntityFactory {
    @JvmStatic
    fun create(version: String): GraphVersionEntity {
        return GraphVersionEntity(version = version, dateCreated = ZonedDateTime.now())
    }
}
