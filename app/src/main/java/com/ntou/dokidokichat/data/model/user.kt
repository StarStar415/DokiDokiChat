package com.ntou.dokidokichat.data.model
import com.ntou.dokidokichat.data.model.Friends

data class user(
    val avatarPath: String = "",
    val email: String = "",
    val friends: List<Friends> = ArrayList<Friends>(),
    val isSignedIn: Boolean = false,
    val name: String = "",
    val password: String = "",
    val userID: String = ""
)