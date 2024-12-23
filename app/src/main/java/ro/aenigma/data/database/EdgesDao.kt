package ro.aenigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ro.aenigma.util.Constants.Companion.EDGES_TABLE

@Dao
interface EdgesDao {

    @Query("DELETE FROM $EDGES_TABLE")
    suspend fun remove()

    @Insert
    suspend fun insert(edges: List<EdgeEntity>)

    @Insert
    suspend fun insert(edge: EdgeEntity)

    @Query("SELECT * FROM $EDGES_TABLE")
    suspend fun get(): List<EdgeEntity>
}
