package ro.aenigma.data.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import ro.aenigma.util.Constants.Companion.GUARDS_TABLE
import java.time.ZonedDateTime

@Entity(
    tableName = GUARDS_TABLE,
    indices = [Index(value = ["address"])]
)
data class GuardEntity (
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    val address: String,
    val publicKey: String,
    val hostname: String,
    val dateCreated: ZonedDateTime
)
