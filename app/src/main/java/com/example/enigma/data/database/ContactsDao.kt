package com.example.enigma.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactsDao {

    @Query("SELECT * FROM Contacts")
    fun getAll(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM Contacts WHERE address = :address")
    fun getContact(address: String): Flow<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact : ContactEntity)
}
