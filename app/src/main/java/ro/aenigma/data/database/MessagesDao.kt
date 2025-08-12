package ro.aenigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import ro.aenigma.util.Constants.Companion.CONVERSATION_PAGE_SIZE
import ro.aenigma.util.Constants.Companion.MESSAGES_TABLE
import kotlinx.coroutines.flow.Flow
import ro.aenigma.util.Constants.Companion.NEWS_FEED_SIZE

@Dao
interface MessagesDao {

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE serverUUID = :serverUUID")
    suspend fun getByServerUuid(serverUUID: String): MessageEntity?

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE refId = :refId")
    suspend fun getByRefId(refId: String): MessageEntity?

    @Transaction
    @Query("SELECT * FROM $MESSAGES_TABLE m WHERE id = :id")
    suspend fun getWithAttachments(id: Long): MessageWithAttachments?

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE deleted = 1 AND chatId = :chatId " +
            "ORDER BY id DESC LIMIT 1")
    fun getLastDeletedFlow(chatId: String): Flow<MessageEntity?>

    @Query("SELECT id FROM $MESSAGES_TABLE WHERE chatId = :chatId ORDER BY id DESC LIMIT 1")
    suspend fun getLastMessageId(chatId: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(message: MessageEntity): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttachment(attachment: AttachmentEntity)

    @Query("UPDATE $MESSAGES_TABLE SET deleted = 1, text = null WHERE chatId = :chatId")
    suspend fun clearConversationSoft(chatId: String)

    @Query("UPDATE $MESSAGES_TABLE SET deleted = 1, text = null WHERE id = :id")
    suspend fun removeSoft(id: Long)

    @Query("UPDATE $MESSAGES_TABLE SET deleted = 1, text = null WHERE refId = :refId")
    suspend fun removeSoft(refId: String?)

    @Transaction
    @Query("SELECT * FROM $MESSAGES_TABLE WHERE chatId = :chatId AND deleted = 0 " +
            "ORDER BY Id DESC LIMIT $CONVERSATION_PAGE_SIZE")
    fun getConversationFlow(chatId: String): Flow<List<MessageWithDetails>>

    @Transaction
    @Query(
        "SELECT * FROM $MESSAGES_TABLE " +
                "WHERE id < :lastIndex " +
                "AND chatId = :chatId " +
                "AND deleted = 0 " +
                "AND (:searchQuery = '' OR text LIKE '%' || :searchQuery || '%') " +
                "ORDER BY id DESC " +
                "LIMIT $CONVERSATION_PAGE_SIZE"
    )
    suspend fun getConversationPage(
        chatId: String,
        lastIndex: Long,
        searchQuery: String = ""
    ): List<MessageWithDetails>

    @Update
    suspend fun update(message: MessageEntity)

    @Transaction
    @Query("SELECT * FROM $MESSAGES_TABLE WHERE type = 'FILES' AND deleted = 0 AND incoming = 1 " +
            "ORDER BY Id DESC LIMIT $NEWS_FEED_SIZE")
    fun getLatestSharedFiles(): Flow<List<MessageWithDetails>>
}
