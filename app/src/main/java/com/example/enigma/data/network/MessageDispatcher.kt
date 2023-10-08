package com.example.enigma.data.network

import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.onion.OnionBuilder
import com.example.enigma.util.Constants.Companion.SERVER_PUBLIC_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageDispatcher @Inject constructor(
    private val client: SignalRClient,
    private val repository: Repository
    ) {

    fun sendMessage(text: String, contact: ContactEntity)
    {
        if(client.isConnected()) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.local.getAddress().collect {
                    val onion = OnionBuilder
                        .create(text.toByteArray())
                        .setAddress(it)
                        .seal(contact.publicKey)
                        .addPeel()
                        .setAddress(contact.address)
                        .seal(SERVER_PUBLIC_KEY)
                        .buildEncode()

                    client.sendMessage(onion)
                }
            }
        }
    }
}
