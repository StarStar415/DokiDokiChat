package com.ntou.dokidokichat.data.model

data class Friend(
    val favor: Int = 50,
    val nickname: String = "",
    val userID: String = "",
    val username: String = "",
    val hasSentMsg: Boolean = false
)