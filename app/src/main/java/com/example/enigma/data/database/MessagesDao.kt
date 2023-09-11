package com.example.enigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.enigma.util.Constants.Companion.MESSAGES_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {

    @Insert
    suspend fun insert(message: MessageEntity)

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE chatId = :chatId")
    fun getConversation(chatId: String) : Flow<List<MessageEntity>>
}
