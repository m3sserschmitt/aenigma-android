/*
    Aenigma - Private Messaging
    Client Android mobile application for Aenigma - Federated messaging system
    Copyright © 2025-2026 Romulus-Emanuel Ruja <romulus-emanuel.ruja@tutanota.com>

    This file is part of Aenigma project.

    Aenigma is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Aenigma is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Aenigma.  If not, see <https://www.gnu.org/licenses/>.
*/

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
