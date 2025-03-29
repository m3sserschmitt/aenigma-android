package ro.aenigma.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import ro.aenigma.util.Constants.Companion.GRAPH_VERSIONS_TABLE
import java.time.ZonedDateTime

@Entity(tableName = GRAPH_VERSIONS_TABLE)
data class GraphVersionEntity (
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    val version: String,
    val dateCreated: ZonedDateTime
)
