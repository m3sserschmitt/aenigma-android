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
import kotlinx.coroutines.flow.Flow
import ro.aenigma.util.Constants.Companion.SERVERS_LIST_SIZE
import ro.aenigma.util.Constants.Companion.VERTICES_TABLE

@Dao
interface VerticesDao {

    @Query("DELETE FROM $VERTICES_TABLE")
    suspend fun remove()

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vertices: List<VertexEntity>)

    @Query("SELECT * FROM $VERTICES_TABLE")
    suspend fun getAll(): List<VertexEntity>

    @Query("SELECT * FROM $VERTICES_TABLE LIMIT $SERVERS_LIST_SIZE")
    suspend fun get(): List<VertexEntity>

    @Query("SELECT * FROM $VERTICES_TABLE LIMIT $SERVERS_LIST_SIZE")
    fun getFlow(): Flow<List<VertexEntity>>

    @Query("SELECT * FROM $VERTICES_TABLE " +
            "WHERE :searchQuery = '' " +
            "OR hostname LIKE '%' || :searchQuery || '%' " +
            "OR onionService LIKE '%' || :searchQuery || '%' " +
            "LIMIT $SERVERS_LIST_SIZE")
    suspend fun search(searchQuery: String): List<VertexEntity>
}
