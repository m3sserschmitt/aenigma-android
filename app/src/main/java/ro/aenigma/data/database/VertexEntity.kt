package ro.aenigma.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import ro.aenigma.util.Constants.Companion.VERTICES_TABLE

@Entity(tableName = VERTICES_TABLE)
class VertexEntity (
    val address: String,
    val publicKey: String,
    val hostname: String?,
) {
    @PrimaryKey(autoGenerate = true) var id: Long = 0
}
