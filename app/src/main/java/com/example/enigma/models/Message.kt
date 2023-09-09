package com.example.enigma.models

import java.util.*

class Message (
    val chatId: String,
    val text: String,
    val incoming: Boolean = true
)
{
    val date: Date = Date()
}
