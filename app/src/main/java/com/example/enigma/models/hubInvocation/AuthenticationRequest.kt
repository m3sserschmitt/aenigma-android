package com.example.enigma.models.hubInvocation

class AuthenticationRequest (
    val publicKey: String,
    val signature: String,
    val syncMessagesOnSuccess: Boolean
)
