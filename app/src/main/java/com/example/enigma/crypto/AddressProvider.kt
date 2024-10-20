package com.example.enigma.crypto

import android.content.Context
import com.example.enigma.util.AddressHelper.Companion.getHexAddressFromPublicKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressProvider @Inject constructor(@ApplicationContext context: Context) {

    private val _address: String?

    private val _publicKey: String? = KeysManager.readPublicKey(context)

    val address: String? get() = _address

    val publicKey: String? get() = _publicKey

    init {
        _address = if(_publicKey != null) getHexAddressFromPublicKey(_publicKey) else null
    }
}
