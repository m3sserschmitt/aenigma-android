package ro.aenigma.data.database

import androidx.room.Embedded
import androidx.room.Relation

data class ContactWithGroup(
    @Embedded val contact: ContactEntity,
    @Relation(parentColumn = "address", entityColumn = "address") val group: GroupEntity?
)
