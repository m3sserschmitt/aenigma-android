package com.example.enigma.onion.contracts

interface IOnionBuilder {
    fun build(): ByteArray
    fun buildEncode(): String
    fun addPeel(): ISetMessageNextAddress
}
