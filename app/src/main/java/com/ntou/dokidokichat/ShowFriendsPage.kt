package com.ntou.dokidokichat

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.ntou.dokidokichat.data.model.Friend

class ShowFriendsPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val UserName = intent.getStringExtra(MainActivity.KEY_USER_NAME)
            ShowFriendsScreen(this, UserName)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowFriendsScreen(activity: Activity, UserName: String?) {
    var friendsList by remember { mutableStateOf(emptyList<Friend>()) }
    val userName = UserName ?: "StarStar415"
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(userName) {
        loadFriends(db, userName) { friends ->
            friendsList = friends
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friends") },
                navigationIcon = {
                    IconButton(onClick = { activity.onBackPressed() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFC1E0))
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            items(friendsList) { friend ->
                FriendItem(friend = friend, userName) {
                    // 更新朋友列表
                    loadFriends(db, userName) { friends ->
                        friendsList = friends
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun FriendItem(friend: Friend, userName: String?, onUpdate: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = friend.nickname,
                fontSize = 22.sp,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(16.dp))
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(color = getFavorColor(friend.favor, 100), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.favor.toString(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        var isEditing by remember { mutableStateOf(false) }
        val newNameField = remember { mutableStateOf(TextFieldValue(friend.nickname)) }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { },
                text = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isEditing) {
                                OutlinedTextField(
                                    value = newNameField.value,
                                    onValueChange = { newNameField.value = it },
                                    label = { Text("編輯好友暱稱") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                IconButton(
                                    onClick = {
                                        // 更新資料庫好友暱稱
                                        val newNickname = newNameField.value.text
                                        updateFriendNickname(db, userName, friend, newNickname) {
                                            Toast.makeText(context, "已更新好友名稱為 $newNickname", Toast.LENGTH_SHORT).show()
                                            isEditing = false
                                            showDialog = false
                                            onUpdate()
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Confirm",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        // 取消修改操作
                                        newNameField.value = TextFieldValue(friend.nickname)
                                        isEditing = false
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = friend.nickname,
                                    modifier = Modifier.weight(1f),
                                    fontSize = 25.sp
                                )
                                IconButton(
                                    onClick = { isEditing = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit",
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                // 前往聊天
                                clickButtonToChat(context, userName, friend.username)
                                showDialog = false
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48FB1)),
                        ) {
                            Text("前往聊天")
                        }
                    }
                },
                confirmButton = { },
                dismissButton = { }
            )
        }
    }
}

fun updateFriendNickname(
    db: FirebaseFirestore,
    userName: String?,
    friend: Friend,
    newNickname: String,
    onComplete: () -> Unit
) {
    db.collection("user").whereEqualTo("username", userName)
        .get(Source.SERVER)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userDoc = task.result.documents[0]
                val user = userDoc.toObject(com.ntou.dokidokichat.data.model.User::class.java)
                if (user != null) {
                    val updatedFriends = user.friends.map { nowFriend ->
                        if (nowFriend.username == friend.username) {
                            nowFriend.copy(nickname = newNickname)
                        } else {
                            nowFriend
                        }
                    }
                    userDoc.reference.update("friends", updatedFriends)
                        .addOnSuccessListener { onComplete() }
                }
            }
        }
}

fun loadFriends(
    db: FirebaseFirestore,
    userName: String,
    onComplete: (List<Friend>) -> Unit
) {
    db.collection("user").whereEqualTo("username", userName)
        .get(Source.SERVER)
        .addOnCompleteListener { task ->
            val friendsList = if (task.isSuccessful) {
                task.result.documents[0].toObject(com.ntou.dokidokichat.data.model.User::class.java)?.friends ?: emptyList()
            } else {
                emptyList()
            }.sortedWith(compareByDescending<Friend> { it.favor }.thenBy { it.nickname })
            onComplete(friendsList)
        }
}

