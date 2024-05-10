package com.example.enigma.data

import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.ContactsDao
import com.example.enigma.data.database.EdgeEntity
import com.example.enigma.data.database.EdgesDao
import com.example.enigma.data.database.GraphPathEntity
import com.example.enigma.data.database.GraphPathsDao
import com.example.enigma.data.database.GraphVersionEntity
import com.example.enigma.data.database.GraphVersionsDao
import com.example.enigma.data.database.GuardEntity
import com.example.enigma.data.database.GuardsDao
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.data.database.MessagesDao
import com.example.enigma.data.database.VertexEntity
import com.example.enigma.data.database.VerticesDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val contactsDao: ContactsDao,
    private val messagesDao: MessagesDao,
    private val guardsDao: GuardsDao,
    private val verticesDao: VerticesDao,
    private val edgesDao: EdgesDao,
    private val graphPathsDao: GraphPathsDao,
    private val graphVersionsDao: GraphVersionsDao
) {
    fun getContacts() : Flow<List<ContactEntity>>
    {
        return contactsDao.get()
    }

    fun searchContacts(searchQuery: String): Flow<List<ContactEntity>>
    {
        return contactsDao.search(searchQuery)
    }

    fun getContact(address: String) : Flow<ContactEntity?>
    {
        return contactsDao.get(address)
    }

    suspend fun insertOrUpdateContact(contactEntity: ContactEntity)
    {
        contactsDao.insertOrUpdate(contactEntity)
    }

    suspend fun insertContact(contactEntity: ContactEntity)
    {
        contactsDao.insert(contactEntity)
    }

    suspend fun updateContact(contactEntity: ContactEntity)
    {
        contactsDao.update(contactEntity)
    }

    suspend fun insertOrUpdateContacts(contacts: List<ContactEntity>): List<Long>
    {
        return contactsDao.insertOrUpdate(contacts)
    }

    suspend fun insertContacts(contacts: List<ContactEntity>): List<Long>
    {
        return contactsDao.insert(contacts)
    }

    suspend fun removeContacts(contacts: List<ContactEntity>)
    {
        contactsDao.remove(contacts)
    }

    fun getConversation(chatId: String) : Flow<List<MessageEntity>>
    {
        return messagesDao.getConversation(chatId)
    }

    fun searchConversation(chatId: String, searchQuery: String): Flow<List<MessageEntity>>
    {
        return messagesDao.searchConversation(chatId, searchQuery)
    }

    suspend fun clearConversation(chatId: String)
    {
        messagesDao.clearConversation(chatId)
    }

    suspend fun removeMessages(messages: List<MessageEntity>)
    {
        messagesDao.remove(messages)
    }

    suspend fun insertMessage(messageEntity: MessageEntity)
    {
        messagesDao.insert(messageEntity)
    }

    suspend fun insertMessages(messageEntities: List<MessageEntity>)
    {
        messagesDao.insert(messageEntities)
    }

    suspend fun markConversationAsUnread(address: String)
    {
        contactsDao.markConversationAsUnread(address)
    }

    suspend fun markConversationAsRead(address: String)
    {
        contactsDao.markConversationAsRead(address)
    }

    fun isGuardAvailable(): Flow<Boolean>
    {
        return guardsDao.isGuardAvailable()
    }

    suspend fun insertGuard(guard: GuardEntity)
    {
        return guardsDao.insert(guard)
    }

    suspend fun getGuard(): GuardEntity?
    {
        return guardsDao.getLastGuard()
    }

    suspend fun getGraphVersion(): GraphVersionEntity?
    {
        return graphVersionsDao.get()
    }

    suspend fun updateGraphVersion(graphVersion: GraphVersionEntity)
    {
        graphVersionsDao.remove()
        graphVersionsDao.insert(graphVersion)
    }

    suspend fun removeVertices()
    {
        return verticesDao.remove()
    }

    suspend fun insertVertices(vertices: List<VertexEntity>)
    {
        return verticesDao.insert(vertices)
    }

    suspend fun getVertices(): List<VertexEntity>
    {
        return verticesDao.get()
    }

    suspend fun removeEdges()
    {
        return edgesDao.remove()
    }

    suspend fun insertEdges(edges: List<EdgeEntity>)
    {
        return edgesDao.insert(edges)
    }

    suspend fun insertEdge(edge: EdgeEntity)
    {
        return edgesDao.insert(edge)
    }

    suspend fun getEdges(): List<EdgeEntity>
    {
        return edgesDao.get()
    }

    fun graphPathExists(destination: String): Flow<Boolean>
    {
        return graphPathsDao.pathExists(destination)
    }

    suspend fun removeGraphPaths()
    {
        graphPathsDao.remove()
    }

    suspend fun insertGraphPaths(paths: List<GraphPathEntity>)
    {
        graphPathsDao.insert(paths)
    }

    suspend fun insertGraphPath(path: GraphPathEntity)
    {
        graphPathsDao.insert(path)
    }

    suspend fun getGraphPath(destination: String): List<GraphPathEntity>
    {
        return graphPathsDao.get(destination)
    }
}
