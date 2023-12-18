package com.example.enigma.data

import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.ContactsDao
import com.example.enigma.data.database.EdgeEntity
import com.example.enigma.data.database.EdgesDao
import com.example.enigma.data.database.GuardEntity
import com.example.enigma.data.database.GuardsDao
import com.example.enigma.data.database.KeyPairEntity
import com.example.enigma.data.database.KeyPairsDao
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
    private val keysDao: KeyPairsDao,
    private val verticesDao: VerticesDao,
    private val edgesDao: EdgesDao
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

    suspend fun insertKeyPair(keyPairEntity: KeyPairEntity)
    {
        keysDao.insert(keyPairEntity)
    }

    fun isKeyAvailable(): Flow<Boolean>
    {
        return keysDao.isKeyAvailable()
    }

    fun getKeys(): Flow<KeyPairEntity>
    {
        return keysDao.getLastKeys()
    }

    fun getPublicKey(): Flow<String>
    {
        return keysDao.getPublicKey()
    }

    fun getAddress(): Flow<String>
    {
        return keysDao.getAddress()
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

    suspend fun insertEdges(vertices: List<EdgeEntity>)
    {
        return edgesDao.insert(vertices)
    }

    fun getEdges(): Flow<List<EdgeEntity>>
    {
        return edgesDao.getAll()
    }
}
