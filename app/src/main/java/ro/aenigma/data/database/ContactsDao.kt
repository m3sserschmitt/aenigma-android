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

import androidx.room.*
import ro.aenigma.util.Constants.Companion.CONTACTS_TABLE
import kotlinx.coroutines.flow.Flow
import ro.aenigma.util.Constants.Companion.CONTACTS_LIST_SIZE

@Dao
interface ContactsDao {
    @Query("SELECT * FROM $CONTACTS_TABLE WHERE address = :address LIMIT 1")
    suspend fun get(address: String): ContactEntity?

    @Query("SELECT * FROM $CONTACTS_TABLE")
    suspend fun getAll(): List<ContactEntity>

    @Query("SELECT * FROM $CONTACTS_TABLE ORDER BY lastMessageId DESC LIMIT $CONTACTS_LIST_SIZE")
    fun getFlow(): Flow<List<ContactEntity>>

    @Transaction
    @Query("SELECT * FROM $CONTACTS_TABLE ORDER BY lastMessageId DESC LIMIT $CONTACTS_LIST_SIZE")
    suspend fun getWithMessages(): List<ContactWithLastMessage>

    @Transaction
    @Query("SELECT * FROM $CONTACTS_TABLE ORDER BY lastMessageId DESC LIMIT $CONTACTS_LIST_SIZE")
    fun getWithMessagesFlow(): Flow<List<ContactWithLastMessage>>

    @Transaction
    @Query("SELECT * FROM $CONTACTS_TABLE WHERE address = :address LIMIT 1")
    suspend fun getWithGroup(address: String): ContactWithGroup?

    @Transaction
    @Query("SELECT * FROM $CONTACTS_TABLE WHERE address = :address")
    fun getWithGroupFlow(address: String): Flow<ContactWithGroup?>

    @Query("SELECT * FROM $CONTACTS_TABLE " +
            "WHERE :searchQuery = '' OR name LIKE '%' || :searchQuery || '%' " +
            "AND :type = '' OR type = :type " +
            "LIMIT $CONTACTS_LIST_SIZE")
    suspend fun search(searchQuery: String, type: String): List<ContactEntity>

    @Query("UPDATE $CONTACTS_TABLE SET hasNewMessage = 1 WHERE address = :address")
    suspend fun markConversationAsUnread(address: String)

    @Query("UPDATE $CONTACTS_TABLE SET hasNewMessage = 0 WHERE address = :address")
    suspend fun markConversationAsRead(address: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(contact : ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(groupEntity: GroupEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(contact: ContactEntity)

    @Update
    suspend fun update(contact: ContactEntity)

    @Delete
    suspend fun remove(contacts: List<ContactEntity>)
}
