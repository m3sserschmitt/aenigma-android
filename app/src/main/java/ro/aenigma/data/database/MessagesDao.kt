package ro.aenigma.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ro.aenigma.util.Constants.Companion.CONVERSATION_PAGE_SIZE
import ro.aenigma.util.Constants.Companion.MESSAGES_TABLE
import kotlinx.coroutines.flow.Flow
import ro.aenigma.models.enums.MessageType
import java.time.ZonedDateTime

@Dao
interface MessagesDao {
    @Query("SELECT * FROM $MESSAGES_TABLE WHERE id = :id AND deleted = 0")
    suspend fun get(id: Long): MessageEntity?

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE serverUUID = :serverUUID")
    suspend fun getByServerUuid(serverUUID: String): MessageEntity?

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE refId = :refId")
    suspend fun getByRefId(refId: String): MessageEntity?

    @Query("SELECT id FROM $MESSAGES_TABLE WHERE chatId = :chatId AND deleted = 0 LIMIT 1")
    suspend fun getLastMessageId(chatId: String): Long

    @Query("INSERT OR IGNORE INTO $MESSAGES_TABLE(" +
            "chatId, senderAddress, serverUUID, text, type, actionFor, refId, incoming, sent," +
            "deleted, date, dateReceivedOnServer, id) " +
            "VALUES(:chatId, :senderAddress, :serverUUID, :text, :type, :actionFor, :refId," +
            ":incoming, 0, 0, :date, :dateReceivedOnServer, " +
                "(SELECT CASE WHEN MIN(id) IS NULL THEN CAST(9223372036854775807 AS INTEGER) ELSE MIN(id) - 1 END AS min_value FROM $MESSAGES_TABLE))"
    )
    suspend fun insertOrIgnore(chatId: String, senderAddress: String?, serverUUID: String?,
                               text: String?, type: MessageType?, actionFor: String?, refId: String?,
                               incoming: Boolean, date: ZonedDateTime, dateReceivedOnServer: ZonedDateTime?
    ): Long?

    @Query("UPDATE $MESSAGES_TABLE SET deleted = 1 WHERE chatId = :chatId")
    suspend fun clearConversationSoft(chatId: String)

    @Query("UPDATE $MESSAGES_TABLE SET deleted = 1 WHERE id IN (:ids)")
    suspend fun removeSoft(ids: List<Long>)

    @Query("UPDATE $MESSAGES_TABLE SET deleted = 1 WHERE refId = :refId")
    suspend fun removeSoft(refId: String?)

    @Transaction
    @Query("SELECT * FROM $MESSAGES_TABLE WHERE chatId = :chatId AND deleted = 0 LIMIT $CONVERSATION_PAGE_SIZE")
    fun getConversationFlow(chatId: String): Flow<List<MessageWithDetails>>

    @Transaction
    @Query(
        "SELECT * FROM $MESSAGES_TABLE " +
                "WHERE id >= :infIndex " +
                "AND chatId = :chatId " +
                "AND deleted = 0 " +
                "AND (:searchQuery = '' OR text LIKE '%' || :searchQuery || '%') " +
                "LIMIT $CONVERSATION_PAGE_SIZE"
    )
    suspend fun getConversation(
        chatId: String,
        infIndex: Long,
        searchQuery: String = ""
    ): List<MessageWithDetails>

    @Update
    suspend fun update(message: MessageEntity)

    @Query("DELETE FROM $MESSAGES_TABLE WHERE deleted = 1")
    suspend fun removeHard()

    @Delete
    suspend fun removeHard(messages: List<MessageEntity>)
}
