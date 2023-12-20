package com.example.enigma.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.enigma.util.Constants.Companion.CONTACTS_TABLE

@Entity(tableName = CONTACTS_TABLE)
data class ContactEntity(
    val address: String,
    val name: String,
    val publicKey: String,
    val guardAddress: String,
    val hasNewMessage: Boolean
)
{
    @PrimaryKey(autoGenerate = true) var id: Long = 0
}
