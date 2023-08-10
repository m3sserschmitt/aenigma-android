package com.example.enigma.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.enigma.util.Constants.Companion.CONTACTS_TABLE

@Entity(tableName = CONTACTS_TABLE)
data class ContactEntity(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val address: String,
    val name: String,
    val hasNewMessage: Boolean
)
