package com.example.enigma.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Update
import com.example.enigma.util.Constants.Companion.CONVERSATION_PAGE_SIZE
import com.example.enigma.util.Constants.Companion.MESSAGES_TABLE
import kotlinx.coroutines.flow.Flow
import java.time.ZonedDateTime

@Dao
interface MessagesDao {
    @Query("SELECT * FROM $MESSAGES_TABLE WHERE id=:id")
    suspend fun get(id: Long): MessageEntity?

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE uuid=:uuid")
    suspend fun get(uuid: String): MessageEntity?

    @Query("INSERT INTO $MESSAGES_TABLE(chatId, text, incoming, date, dateReceivedOnServer, sent, uuid, id) " +
            "VALUES(:chatId, :text, :incoming, :date, :dateReceivedOnServer, :sent, :uuid, " +
            "(SELECT CASE WHEN MIN(id) IS NULL THEN CAST(9223372036854775807 AS INTEGER) ELSE MIN(id) - 1 END AS min_value FROM $MESSAGES_TABLE))")
    suspend fun insert(chatId: String, text: String, incoming: Boolean, sent: Boolean, date: ZonedDateTime, dateReceivedOnServer: ZonedDateTime?, uuid: String?): Long

    @Query("DELETE FROM $MESSAGES_TABLE WHERE chatId = :chatId")
    suspend fun clearConversation(chatId: String)

    @Delete
    suspend fun remove(messages: List<MessageEntity>)

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE chatId = :chatId LIMIT $CONVERSATION_PAGE_SIZE")
    fun getConversation(chatId: String) : Flow<List<MessageEntity>>

    @Query("SELECT * FROM $MESSAGES_TABLE " +
            "WHERE id > :infIndex " +
            "AND chatId = :chatId " +
            "AND (:searchQuery = '' OR text LIKE '%' || :searchQuery || '%') " +
            "LIMIT $CONVERSATION_PAGE_SIZE")
    suspend fun getConversation(chatId: String, infIndex: Long, searchQuery: String = ""): List<MessageEntity>

    @Query("SELECT * FROM " +
            "(SELECT * FROM $MESSAGES_TABLE LIMIT 100) " +
            "WHERE sent = 0 AND incoming = 0")
    fun getOutgoingMessages(): Flow<List<MessageEntity>>

    @Update
    suspend fun update(message: MessageEntity)
}
