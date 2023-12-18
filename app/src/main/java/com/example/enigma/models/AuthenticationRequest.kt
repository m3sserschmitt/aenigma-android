package com.example.enigma.models

class AuthenticationRequest (
    val publicKey: String,
    val signature: String,
    val syncMessagesOnSuccess: Boolean,
    val updateNetworkGraph: Boolean)
