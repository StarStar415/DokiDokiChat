package com.ntou.dokidokichat.data.model

import com.google.firebase.Timestamp

data class Chat(
    val content: String = "",
    val member: List<String> = emptyList(),
    val sendTime: Timestamp = Timestamp.now(),
    val senderUsername: String = "",
    val type: String = "text"
)
