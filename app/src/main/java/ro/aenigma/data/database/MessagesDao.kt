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

@Dao
interface MessagesDao {
    @Query("SELECT * FROM $MESSAGES_TABLE WHERE id = :id")
    suspend fun get(id: Long): MessageEntity?

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE serverUUID = :serverUUID")
    suspend fun getByServerUuid(serverUUID: String): MessageEntity?

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE refId = :refId")
    suspend fun getByRefId(refId: String): MessageEntity?

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE deleted = 1 AND chatId = :chatId ORDER BY id DESC LIMIT 1")
    fun getLastDeletedFlow(chatId: String): Flow<MessageEntity?>

    @Query("SELECT id FROM $MESSAGES_TABLE WHERE chatId = :chatId AND deleted = 0 ORDER BY id DESC LIMIT 1")
    suspend fun getLastMessageId(chatId: String): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(message: MessageEntity): Long?

    @Query("UPDATE $MESSAGES_TABLE SET deleted = 1, text = null WHERE chatId = :chatId")
    suspend fun clearConversationSoft(chatId: String)

    @Query("UPDATE $MESSAGES_TABLE SET deleted = 1, text = null WHERE id IN (:ids)")
    suspend fun removeSoft(ids: List<Long>)

    @Query("UPDATE $MESSAGES_TABLE SET deleted = 1, text = null WHERE refId = :refId")
    suspend fun removeSoft(refId: String?)

    @Transaction
    @Query("SELECT * FROM $MESSAGES_TABLE WHERE chatId = :chatId AND deleted = 0 ORDER BY Id DESC LIMIT $CONVERSATION_PAGE_SIZE")
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

    @Query("DELETE FROM $MESSAGES_TABLE WHERE deleted = 1")
    suspend fun removeHard()
}
