package com.ntou.dokidokichat

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.auth.User
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
    var friendsList: List<Friend> by remember{ mutableStateOf(emptyList())}
    val userName = UserName ?: "StarStar415"
    val db = FirebaseFirestore.getInstance()

    fun fetchFriends() {
        db.collection("user").whereEqualTo("username", userName).get(Source.SERVER)
            .addOnCompleteListener { task ->
                friendsList = if (task.isSuccessful) {
                    task.result.documents[0]
                        .toObject(com.ntou.dokidokichat.data.model.User::class.java)?.friends
                        ?: emptyList()
                } else {
                    emptyList()
                }.sortedWith(compareByDescending<Friend> { it.favor }.thenBy { it.nickname })
            }
    }

    fetchFriends()

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
                FriendItem(friend = friend , UserName){
                    // 更新朋友列表
                    fetchFriends()
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun FriendItem(friend: Friend , userName: String?, onUpdate: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showDialog = true
                }
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
                    title = {

                    },
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
                                            db.collection("user").whereEqualTo("username", userName)
                                                .get(Source.SERVER)
                                                .addOnCompleteListener { task ->
                                                    if (task.isSuccessful) {
                                                        val userDoc = task.result.documents[0]
                                                        val user = userDoc.toObject(com.ntou.dokidokichat.data.model.User::class.java)
                                                        if (user != null) {
                                                            val updatedFriends = user.friends.map { nowfriend ->
                                                                if (nowfriend.username == friend.username) {
                                                                    nowfriend.copy(nickname = newNickname)
                                                                } else {
                                                                    nowfriend
                                                                }
                                                            }
                                                            userDoc.reference.update("friends", updatedFriends)
                                                        }
                                                    }
                                                }
                                            Toast.makeText(context, "已更新好友名稱為 $newNickname", Toast.LENGTH_SHORT).show()
                                            isEditing = false
                                            onUpdate()
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
                    confirmButton = {

                    },
                    dismissButton = {

                    }
                )
            }

        }
}


