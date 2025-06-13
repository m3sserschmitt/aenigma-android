package ro.aenigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ro.aenigma.util.Constants.Companion.EDGES_TABLE

@Dao
interface EdgesDao {

    @Query("DELETE FROM $EDGES_TABLE")
    suspend fun remove()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(edges: List<EdgeEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(edge: EdgeEntity)

    @Query("SELECT * FROM $EDGES_TABLE")
    suspend fun get(): List<EdgeEntity>
}
