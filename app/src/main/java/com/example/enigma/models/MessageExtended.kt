package com.example.enigma.models

class MessageExtended(text: String, val publicKey: String, val guardHostname: String)
    : MessageBase(text)
