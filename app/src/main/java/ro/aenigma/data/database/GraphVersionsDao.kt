package ro.aenigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ro.aenigma.util.Constants.Companion.GRAPH_VERSIONS_TABLE

@Dao
interface GraphVersionsDao {

    @Insert
    suspend fun insert(graphVersion: GraphVersionEntity)

    @Query("DELETE FROM $GRAPH_VERSIONS_TABLE")
    suspend fun remove()

    @Query("SELECT * FROM $GRAPH_VERSIONS_TABLE ORDER BY id DESC LIMIT 1")
    suspend fun get(): GraphVersionEntity?
}
