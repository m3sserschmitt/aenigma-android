package ro.aenigma.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import ro.aenigma.models.GroupData
import ro.aenigma.util.Constants.Companion.GROUPS_TABLE

@Entity(
    tableName = GROUPS_TABLE,
    foreignKeys = [ForeignKey(
        entity = ContactEntity::class,
        parentColumns = ["address"],
        childColumns = ["address"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class GroupEntity (
    @PrimaryKey
    val address: String,
    val groupData: GroupData,
    val resourceUrl: String
)
