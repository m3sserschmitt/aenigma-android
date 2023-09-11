package com.example.enigma.onion.contracts

interface ISetMessageNextAddress {
    fun setAddress(address: String): IEncryptMessage
}
