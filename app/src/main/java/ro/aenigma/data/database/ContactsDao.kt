package ro.aenigma.data.database

import androidx.room.*
import ro.aenigma.util.Constants.Companion.CONTACTS_TABLE
import kotlinx.coroutines.flow.Flow
import ro.aenigma.util.Constants.Companion.CONTACTS_LIST_SIZE

@Dao
interface ContactsDao {
    @Query("SELECT * FROM $CONTACTS_TABLE WHERE address = :address LIMIT 1")
    suspend fun get(address: String): ContactEntity?

    @Query("SELECT * FROM $CONTACTS_TABLE")
    suspend fun getAll(): List<ContactEntity>

    @Query("SELECT * FROM $CONTACTS_TABLE ORDER BY lastMessageId DESC LIMIT $CONTACTS_LIST_SIZE")
    fun getFlow(): Flow<List<ContactEntity>>

    @Transaction
    @Query("SELECT * FROM $CONTACTS_TABLE ORDER BY lastMessageId DESC LIMIT $CONTACTS_LIST_SIZE")
    suspend fun getWithMessages(): List<ContactWithLastMessage>

    @Transaction
    @Query("SELECT * FROM $CONTACTS_TABLE ORDER BY lastMessageId DESC LIMIT $CONTACTS_LIST_SIZE")
    fun getWithMessagesFlow(): Flow<List<ContactWithLastMessage>>

    @Transaction
    @Query("SELECT * FROM $CONTACTS_TABLE WHERE address = :address LIMIT 1")
    suspend fun getWithGroup(address: String): ContactWithGroup?

    @Transaction
    @Query("SELECT * FROM $CONTACTS_TABLE WHERE address = :address")
    fun getWithGroupFlow(address: String): Flow<ContactWithGroup?>

    @Query("SELECT * FROM $CONTACTS_TABLE " +
            "WHERE :searchQuery = '' OR name LIKE '%' || :searchQuery || '%' " +
            "AND :type = '' OR type = :type " +
            "LIMIT $CONTACTS_LIST_SIZE")
    suspend fun search(searchQuery: String, type: String): List<ContactEntity>

    @Query("UPDATE $CONTACTS_TABLE SET hasNewMessage = 1 WHERE address = :address")
    suspend fun markConversationAsUnread(address: String)

    @Query("UPDATE $CONTACTS_TABLE SET hasNewMessage = 0 WHERE address = :address")
    suspend fun markConversationAsRead(address: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(contact : ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(groupEntity: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(contact: ContactEntity)

    @Update
    suspend fun update(contact: ContactEntity)

    @Delete
    suspend fun remove(contacts: List<ContactEntity>)
}
