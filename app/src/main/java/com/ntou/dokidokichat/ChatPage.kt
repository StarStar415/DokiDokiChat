package com.ntou.dokidokichat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ntou.dokidokichat.ui.theme.DokiDokiChatTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class ChatPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val UserName = intent.getStringExtra(MainActivity.KEY_USER_NAME)
            val FriendName = intent.getStringExtra(FriendsPage.FRIEND_NAME)

            ShowChatScreen(UserName, FriendName, onBackPressed = { finish() })
        }
    }
}


data class Message(
    val content: String,
    val sentByUser: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowChatScreen(userName: String?, friendName: String?, onBackPressed: () -> Unit) {
    var messages by remember {
        mutableStateOf(
            listOf(
                Message("hi", false),
                Message("安安", true)
            )
        )
    }
    var messageText by remember { mutableStateOf(TextFieldValue("")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Chat with $friendName") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    reverseLayout = false,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(messages) { message ->
                        if (message.sentByUser) {
                            // Sent message
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = message.content,
                                    color = Color.White,
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                        .padding(8.dp)
                                )
                            }
                        } else {
                            // Received message
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.Start
                            ) {
                                Text(
                                    text = message.content,
                                    color = Color.Black,
                                    modifier = Modifier
                                        .background(Color.LightGray, shape = CircleShape)
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Enter message") }
                    )
                    Button(
                        onClick = {
                            if (messageText.text.isNotBlank()) {
                                messages = messages + (Message(messageText.text, true))
                                messageText = TextFieldValue("")
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    )
}
