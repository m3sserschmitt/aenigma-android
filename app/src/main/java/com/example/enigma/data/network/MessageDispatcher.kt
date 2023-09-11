package com.example.enigma.data.network

import com.example.enigma.data.database.ContactEntity
import com.example.enigma.onion.OnionBuilder
import com.example.enigma.util.Constants.Companion.HEX_ADDRESS
import com.example.enigma.util.Constants.Companion.SERVER_PUBLIC_KEY
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageDispatcher @Inject constructor(private val client: SignalRClient) {

    fun sendMessage(text: String, contact: ContactEntity)
    {
        if(client.isConnected())
        {
            val onion = OnionBuilder
                .create(text.toByteArray())
                .setAddress(HEX_ADDRESS)
                .seal(contact.publicKey)
                .addPeel()
                .setAddress(contact.address)
                .seal(SERVER_PUBLIC_KEY)
                .buildEncode()

            client.sendMessage(onion)
        }
    }
}
