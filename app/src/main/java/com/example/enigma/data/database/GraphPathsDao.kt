package com.example.enigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.enigma.util.Constants.Companion.GRAPH_PATHS_TABLE
import kotlinx.coroutines.flow.Flow

@Dao
interface GraphPathsDao {

    @Query("DELETE FROM $GRAPH_PATHS_TABLE")
    suspend fun remove()

    @Insert
    suspend fun insert(paths: List<GraphPathEntity>)

    @Insert
    suspend fun insert(path: GraphPathEntity)

    @Query("SELECT * FROM $GRAPH_PATHS_TABLE WHERE destination = :destination")
    suspend fun get(destination: String): List<GraphPathEntity>

    @Query("SELECT EXISTS (SELECT * FROM $GRAPH_PATHS_TABLE WHERE destination = :destination LIMIT 1)")
    fun pathExists(destination: String): Flow<Boolean>
}