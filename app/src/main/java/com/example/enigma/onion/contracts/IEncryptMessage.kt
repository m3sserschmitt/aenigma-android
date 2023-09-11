package com.example.enigma.onion.contracts

interface IEncryptMessage {
    fun seal(key: String): IOnionBuilder
}
