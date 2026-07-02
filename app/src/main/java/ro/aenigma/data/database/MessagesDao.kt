/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

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
import ro.aenigma.util.Constants.Companion.BROADCAST_CONTACT_ADDRESS

@Dao
interface MessagesDao {

    @Query("SELECT * FROM $MESSAGES_TABLE WHERE Id = :id LIMIT 1")
    suspend fun get(id: Long): MessageEntity?

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
    @Query("SELECT * FROM $MESSAGES_TABLE " +
            "WHERE id < :lastIndex " +
            "AND deleted = 0 " +
            "AND type = 'FILES' " +
            "AND (incoming = 1 OR chatId = '$BROADCAST_CONTACT_ADDRESS') " +
            "ORDER BY Id DESC LIMIT $CONVERSATION_PAGE_SIZE")
    suspend fun getSharedFiles(lastIndex: Long): List<MessageWithDetails>
}
