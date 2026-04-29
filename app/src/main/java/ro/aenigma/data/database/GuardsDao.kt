package ro.aenigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ro.aenigma.util.Constants.Companion.GUARDS_HISTORY_SIZE
import ro.aenigma.util.Constants.Companion.GUARDS_TABLE
import ro.aenigma.util.Constants.Companion.SERVERS_LIST_SIZE

@Dao
interface GuardsDao {

    @Query("SELECT COUNT(*) FROM $GUARDS_TABLE ")
    suspend fun count(): Int

    @Query("DELETE FROM $GUARDS_TABLE WHERE id IN (SELECT id FROM $GUARDS_TABLE ORDER BY id ASC LIMIT :n)")
    suspend fun deleteOldest(n: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(guard: GuardEntity)

    @Transaction
    suspend fun insertWithLimit(guard: GuardEntity) {
        val c = count()
        if (c >= GUARDS_HISTORY_SIZE) {
            val toDelete = c - (GUARDS_HISTORY_SIZE - 1)
            deleteOldest(toDelete)
        }
        insert(guard)
    }

    @Query("SELECT * FROM $GUARDS_TABLE ORDER BY id DESC LIMIT 1")
    suspend fun getGuard(): GuardEntity?

    @Query("SELECT * FROM $GUARDS_TABLE ORDER BY id DESC LIMIT $GUARDS_HISTORY_SIZE")
    suspend fun get(): List<GuardEntity>

    @Query("SELECT * FROM $GUARDS_TABLE ORDER BY id DESC LIMIT $GUARDS_HISTORY_SIZE")
    fun getFlow(): Flow<List<GuardEntity>>

    @Query("SELECT * FROM $GUARDS_TABLE " +
            "WHERE :searchQuery = '' " +
            "OR hostname LIKE '%' || :searchQuery || '%' " +
            "OR onionService LIKE '%' || :searchQuery || '%' " +
            "ORDER BY id DESC " +
            "LIMIT $SERVERS_LIST_SIZE")
    suspend fun search(searchQuery: String): List<GuardEntity>
}
