package com.example.enigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.enigma.util.Constants.Companion.GUARDS_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface GuardsDao {

    @Insert
    suspend fun insert(guard: GuardEntity)

    @Query("SELECT EXISTS(SELECT * FROM $GUARDS_TABLE)")
    fun isGuardAvailable(): Flow<Boolean>

    @Query("SELECT * FROM $GUARDS_TABLE ORDER BY id DESC LIMIT 1")
    suspend fun getLastGuard(): GuardEntity?
}
