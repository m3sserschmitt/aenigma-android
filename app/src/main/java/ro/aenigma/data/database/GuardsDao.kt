package ro.aenigma.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ro.aenigma.util.Constants.Companion.GUARDS_TABLE

@Dao
interface GuardsDao {

    @Insert
    suspend fun insert(guard: GuardEntity)

    @Query("SELECT * FROM $GUARDS_TABLE ORDER BY id DESC LIMIT 1")
    suspend fun getLastGuard(): GuardEntity?
}
