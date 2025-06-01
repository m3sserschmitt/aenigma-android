package ro.aenigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ro.aenigma.util.Constants.Companion.VERTICES_TABLE

@Dao
interface VerticesDao {

    @Query("DELETE FROM $VERTICES_TABLE")
    suspend fun remove()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vertices: List<VertexEntity>)

    @Query("SELECT * FROM $VERTICES_TABLE")
    suspend fun get(): List<VertexEntity>
}
