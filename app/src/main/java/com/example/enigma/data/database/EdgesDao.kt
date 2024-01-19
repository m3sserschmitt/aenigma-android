package com.example.enigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.enigma.util.Constants.Companion.EDGES_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface EdgesDao {

    @Query("DELETE FROM $EDGES_TABLE")
    suspend fun remove()

    @Insert
    suspend fun insert(edges: List<EdgeEntity>)

    @Insert
    suspend fun insert(edge: EdgeEntity)

    @Query("SELECT * FROM $EDGES_TABLE")
    fun getAll(): Flow<List<EdgeEntity>>
}
