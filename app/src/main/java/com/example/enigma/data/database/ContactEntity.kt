package com.example.enigma.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.enigma.util.Constants.Companion.CONTACTS_TABLE

@Entity(tableName = CONTACTS_TABLE)
data class ContactEntity(
    @PrimaryKey val address: String,
    val name: String,
    val publicKey: String,
    val guardHostname: String,
    val hasNewMessage: Boolean
)
