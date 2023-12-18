package com.example.enigma.models

import java.util.Date

class PendingMessage(
    val destination: String,
    val content: String,
    val dateReceived: Date
)
