package com.example.enigma.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.enigma.util.Constants

@Entity(tableName = Constants.KEYS_TABLE)
data class KeyPairEntity (
    val publicKey: String,
    val privateKey: String,
    val address: String
) {
    @PrimaryKey(autoGenerate = true) var id: Long = 0
}
