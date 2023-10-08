package com.example.enigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.enigma.util.Constants.Companion.KEYS_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface KeyPairsDao {
    @Insert
    suspend fun insert(keyPairEntity: KeyPairEntity)

    @Query("SELECT * FROM $KEYS_TABLE ORDER BY id DESC LIMIT 1")
    fun getLastKeys(): Flow<KeyPairEntity>

    @Query("SELECT PublicKey FROM $KEYS_TABLE ORDER BY id DESC LIMIT 1")
    fun getPublicKey(): Flow<String>

    @Query("SELECT EXISTS(SELECT * FROM $KEYS_TABLE)")
    fun isKeyAvailable(): Flow<Boolean>

    @Query("SELECT Address FROM $KEYS_TABLE ORDER BY id DESC LIMIT 1")
    fun getAddress(): Flow<String>
}
