package com.ntou.dokidokichat.data.model

data class User(
    val avatarPath: String = "",
    val email: String = "",
    var friends: List<Friend> = emptyList(),
    val isSignedIn: Boolean = false,
    val name: String = "",
    val password: String = "",
    val userID: String = "",
    val username: String = ""
)