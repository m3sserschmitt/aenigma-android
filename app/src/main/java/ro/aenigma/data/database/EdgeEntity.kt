package ro.aenigma.data.database

import androidx.room.Entity
import ro.aenigma.util.Constants.Companion.EDGES_TABLE

@Entity(
    tableName = EDGES_TABLE,
    primaryKeys = ["sourceAddress", "targetAddress"]
)
data class EdgeEntity (
    val sourceAddress: String,
    val targetAddress: String
)
