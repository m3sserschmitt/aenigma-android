package com.example.enigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.enigma.util.Constants.Companion.VERTICES_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface VerticesDao {

    @Query("DELETE FROM $VERTICES_TABLE")
    suspend fun remove()

    @Insert
    suspend fun insert(vertices: List<VertexEntity>)

    @Query("SELECT * FROM $VERTICES_TABLE")
    fun getAll(): Flow<List<VertexEntity>>
}
