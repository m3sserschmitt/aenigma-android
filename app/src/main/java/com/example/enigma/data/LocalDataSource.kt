package com.example.enigma.data

import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.ContactsDao
import com.example.enigma.data.database.EdgeEntity
import com.example.enigma.data.database.EdgesDao
import com.example.enigma.data.database.GraphPathEntity
import com.example.enigma.data.database.GraphPathsDao
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
    private val graphPathsDao: GraphPathsDao
){
    fun getContacts() : Flow<List<ContactEntity>>
    {
        return contactsDao.getAll()
    }

    fun getContact(address: String) : Flow<ContactEntity>
    {
        return contactsDao.getContact(address)
    }

    suspend fun insertContact(contactEntity: ContactEntity)
    {
        contactsDao.insert(contactEntity)
    }

    suspend fun insertContacts(contacts: List<ContactEntity>): List<Long>
    {
        return contactsDao.insert(contacts)
    }

    fun getConversation(chatId: String) : Flow<List<MessageEntity>>
    {
        return messagesDao.getConversation(chatId)
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

    fun getGuard(): Flow<GuardEntity>
    {
        return guardsDao.getLastGuard()
    }

    suspend fun removeVertices()
    {
        return verticesDao.remove()
    }

    suspend fun insertVertices(vertices: List<VertexEntity>)
    {
        return verticesDao.insert(vertices)
    }

    fun getVertices(): Flow<List<VertexEntity>>
    {
        return verticesDao.getAll()
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

    fun getEdges(): Flow<List<EdgeEntity>>
    {
        return edgesDao.getAll()
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
