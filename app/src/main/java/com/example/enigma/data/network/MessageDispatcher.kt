package com.example.enigma.data.network

import com.example.enigma.crypto.AddressProvider
import com.example.enigma.data.Repository
import com.example.enigma.data.database.ContactEntity
import com.example.enigma.data.database.MessageEntity
import com.example.enigma.onion.OnionBuilder
import com.example.enigma.util.Constants.Companion.SERVER_PUBLIC_KEY
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class MessageDispatcher @Inject constructor(
    private val client: SignalRClient,
    private val repository: Repository,
    private val addressProvider: AddressProvider
    ) {

    fun sendMessage(text: String, contact: ContactEntity)
    {
        if(client.isConnected()) {
            CoroutineScope(Dispatchers.IO).launch {
                if (addressProvider.address != null) {
                    val onion = OnionBuilder
                        .create(text.toByteArray())
                        .setAddress(addressProvider.address!!)
                        .seal(contact.publicKey)
                        .addPeel()
                        .setAddress(contact.address)
                        .seal(SERVER_PUBLIC_KEY)
                        .buildEncode()

                    client.sendMessage(onion)
                    repository.local.insertMessage(
                        MessageEntity(
                            contact.address,
                            text,
                            false,
                            Date()
                        )
                    )
                }
            }
        }
    }
}
