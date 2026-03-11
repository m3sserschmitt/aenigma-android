package ro.aenigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ro.aenigma.util.Constants.Companion.SERVERS_LIST_MAX_COUNT
import ro.aenigma.util.Constants.Companion.VERTICES_TABLE

@Dao
interface VerticesDao {

    @Query("DELETE FROM $VERTICES_TABLE")
    suspend fun remove()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vertices: List<VertexEntity>)

    @Query("SELECT * FROM $VERTICES_TABLE")
    suspend fun getAll(): List<VertexEntity>

    @Query("SELECT * FROM $VERTICES_TABLE LIMIT $SERVERS_LIST_MAX_COUNT")
    suspend fun get(): List<VertexEntity>

    @Query("SELECT * FROM $VERTICES_TABLE LIMIT $SERVERS_LIST_MAX_COUNT")
    fun getFlow(): Flow<List<VertexEntity>>

    @Query("SELECT * FROM $VERTICES_TABLE " +
            "WHERE :searchQuery = '' " +
            "OR hostname LIKE '%' || :searchQuery || '%' " +
            "OR onionService LIKE '%' || :searchQuery || '%' " +
            "LIMIT $SERVERS_LIST_MAX_COUNT")
    suspend fun search(searchQuery: String): List<VertexEntity>
}
