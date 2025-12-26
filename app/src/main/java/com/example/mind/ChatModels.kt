package com.example.mind

import java.util.UUID

data class Message(
     val id: String = UUID.randomUUID().toString(),
     val text: String,
     val isFromUser: Boolean,
     val timestamp: String
)

data class ChatItem(
     val id: String = UUID.randomUUID().toString(),
     val name: String,
     val isAI: Boolean = false,
     val messages: List<Message> = emptyList()
)