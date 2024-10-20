package com.example.enigma.data.database

import androidx.room.*
import com.example.enigma.util.Constants.Companion.CONTACTS_TABLE
import com.example.enigma.util.Constants.Companion.MESSAGES_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {

    @Query("SELECT * FROM $CONTACTS_TABLE")
    fun get(): Flow<List<ContactEntity>>

    @Query(
        "SELECT c.address, c.name, c.guardHostname, c.guardAddress, c.publicKey, c.hasNewMessage, c.lastSynchronized, c.lastMessageId," +
                "m.text AS lastMessageText, m.incoming AS lastMessageIncoming " +
                "FROM $CONTACTS_TABLE c LEFT JOIN $MESSAGES_TABLE m ON m.id = c.lastMessageId "
    )
    fun getWithConversationPreviewFlow(): Flow<List<ContactWithConversationPreview>>

    @Query(
        "SELECT c.address, c.name, c.guardHostname, c.guardAddress, c.publicKey, c.hasNewMessage, c.lastSynchronized, c.lastMessageId," +
                "m.text AS lastMessageText, m.incoming AS lastMessageIncoming " +
                "FROM $CONTACTS_TABLE c LEFT JOIN $MESSAGES_TABLE m ON m.id = c.lastMessageId "
    )
    suspend fun getWithConversationPreview(): List<ContactWithConversationPreview>

    @Query("SELECT * FROM $CONTACTS_TABLE WHERE address = :address LIMIT 1")
    suspend fun get(address: String): ContactEntity?

    @Query("SELECT * FROM $CONTACTS_TABLE WHERE address = :address LIMIT 1")
    fun getFlow(address: String): Flow<ContactEntity?>

    @Query("SELECT * FROM $CONTACTS_TABLE WHERE :searchQuery = '' OR name LIKE '%' || :searchQuery || '%'")
    suspend fun search(searchQuery: String): List<ContactEntity>

    @Query("UPDATE $CONTACTS_TABLE SET hasNewMessage = 1 WHERE address = :address")
    suspend fun markConversationAsUnread(address: String)

    @Query("UPDATE $CONTACTS_TABLE SET hasNewMessage = 0 WHERE address = :address")
    suspend fun markConversationAsRead(address: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(contact : ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(contact : List<ContactEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(contact : List<ContactEntity>): List<Long>

    @Update
    suspend fun update(contact: ContactEntity)

    @Delete
    suspend fun remove(contacts: List<ContactEntity>)
}
