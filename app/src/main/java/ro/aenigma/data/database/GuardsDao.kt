package ro.aenigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import ro.aenigma.util.Constants.Companion.GUARDS_HISTORY_MAX_COUNT
import ro.aenigma.util.Constants.Companion.GUARDS_TABLE

@Dao
interface GuardsDao {

    @Query("SELECT COUNT(*) FROM $GUARDS_TABLE ")
    suspend fun count(): Int

    @Query("DELETE FROM $GUARDS_TABLE WHERE id IN (SELECT id FROM $GUARDS_TABLE ORDER BY id ASC LIMIT :n)")
    suspend fun deleteOldest(n: Int)

    @Insert
    suspend fun insert(guard: GuardEntity)

    @Transaction
    suspend fun insertWithLimit(guard: GuardEntity) {
        val c = count()
        if (c >= GUARDS_HISTORY_MAX_COUNT) {
            val toDelete = c - (GUARDS_HISTORY_MAX_COUNT - 1)
            deleteOldest(toDelete)
        }
        insert(guard)
    }

    @Query("SELECT * FROM $GUARDS_TABLE ORDER BY id DESC LIMIT 1")
    suspend fun getLastGuard(): GuardEntity?
}
