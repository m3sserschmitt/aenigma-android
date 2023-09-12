package com.example.enigma.data.database

import androidx.room.*
import com.example.enigma.util.Constants.Companion.CONTACTS_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {

    @Query("SELECT * FROM $CONTACTS_TABLE")
    fun getAll(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM $CONTACTS_TABLE WHERE address = :address LIMIT 1")
    fun getContact(address: String): Flow<ContactEntity>

    @Query("UPDATE $CONTACTS_TABLE SET hasNewMessage = true WHERE address = :address")
    suspend fun markConversationAsUnread(address: String)

    @Query("UPDATE $CONTACTS_TABLE SET hasNewMessage = false WHERE address = :address")
    suspend fun markConversationAsRead(address: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact : ContactEntity)
}
