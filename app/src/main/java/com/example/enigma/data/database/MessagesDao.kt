package com.example.enigma.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.enigma.util.Constants.Companion.MESSAGES_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {

    @Insert
    suspend fun insert(message: MessageEntity)

    @Insert
    suspend fun insert(messages: List<MessageEntity>)

    @Query("DELETE FROM $MESSAGES_TABLE WHERE chatId = :chatId")
    suspend fun clearConversation(chatId: String)

    @Delete
    suspend fun remove(messages: List<MessageEntity>)

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE chatId = :chatId")
    fun getConversation(chatId: String) : Flow<List<MessageEntity>>

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE chatId = :chatId AND (:searchQuery = '' OR text LIKE '%' || :searchQuery || '%')")
    fun searchConversation(chatId: String, searchQuery: String): Flow<List<MessageEntity>>
}
