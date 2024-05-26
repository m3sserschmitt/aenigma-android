package com.example.enigma.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import com.example.enigma.util.Constants.Companion.CONVERSATION_PAGE_SIZE
import com.example.enigma.util.Constants.Companion.MESSAGES_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface MessagesDao {

    @Query("INSERT INTO $MESSAGES_TABLE(chatId, text, incoming, date, id)" +
            "VALUES(:chatId, :text, :incoming, :date, (" +
            "SELECT CASE WHEN MIN(id) IS NULL THEN CAST(9223372036854775807 AS INTEGER) ELSE MIN(id) - 1 END AS min_value FROM $MESSAGES_TABLE)" +
            ")")
    suspend fun insert(chatId: String, text: String, incoming: Boolean, date: String)

    @Query("DELETE FROM $MESSAGES_TABLE WHERE chatId = :chatId")
    suspend fun clearConversation(chatId: String)

    @Delete
    suspend fun remove(messages: List<MessageEntity>)

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE chatId = :chatId LIMIT $CONVERSATION_PAGE_SIZE")
    fun getConversation(chatId: String) : Flow<List<MessageEntity>>

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE id > :infIndex AND chatId = :chatId AND (:searchQuery = '' OR text LIKE '%' || :searchQuery || '%') LIMIT $CONVERSATION_PAGE_SIZE")
    suspend fun getConversation(chatId: String, infIndex: Long, searchQuery: String = ""): List<MessageEntity>
}
