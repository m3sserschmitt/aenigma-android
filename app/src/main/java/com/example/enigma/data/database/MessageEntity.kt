package com.example.enigma.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.enigma.util.Constants
import java.util.*

@Entity(tableName = Constants.MESSAGES_TABLE)
data class MessageEntity (
    val chatId: String,
    val text: String,
    val incoming: Boolean,
    val date: Date
)
{
    @PrimaryKey(autoGenerate = true) var id: Long = 0
}
