package com.example.enigma.data.database

import androidx.room.*
import com.example.enigma.util.Constants.Companion.CONTACTS_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {

    @Query("SELECT * FROM $CONTACTS_TABLE")
    fun getAll(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM Contacts WHERE address = :address LIMIT 1")
    fun getContact(address: String): Flow<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact : ContactEntity)
}
