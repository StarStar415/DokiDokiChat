package com.ntou.dokidokichat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObject
import com.ntou.dokidokichat.data.model.Chat
import com.ntou.dokidokichat.data.model.Friend
import com.ntou.dokidokichat.data.model.User

class ChatPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userName = intent.getStringExtra(MainActivity.KEY_USER_NAME)
            val friendUserName = intent.getStringExtra(FriendsPage.FRIEND_USERNAME)

            ShowChatScreen(userName!!, friendUserName!!, onBackPressed = { finish() })
        }
    }
}

data class Message(
    val content: String,
    val sentByUser: Boolean,
    val time: Timestamp = Timestamp.now()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowChatScreen(userName: String, friendUserName: String, onBackPressed: () -> Unit) {
    var messages by remember {
        mutableStateOf(
            emptyList<Message>()
        )
    }
    var messageText by remember { mutableStateOf(TextFieldValue("")) }
    val listState = rememberLazyListState()
    val db = FirebaseFirestore.getInstance()
    var oldMsgFlag by remember { mutableStateOf(false)}
    var newMsgFlag by remember { mutableStateOf(false)}
    var count by remember{ mutableLongStateOf(1)}
    var oldVisibleItemIdx by remember { mutableIntStateOf(1) }
    var oldVisibleItemOffset by remember { mutableIntStateOf(1) }
    lateinit var friendData: User
    var friend_nickName by remember{ mutableStateOf("")}
    db.collection("user").whereEqualTo("username", friendUserName).get()
        .addOnCompleteListener(){task->
            friendData = task.result.documents[0].toObject(User::class.java)!!
            friend_nickName = friendData.name
        }
    val msgOldRefresh: () -> Unit = {
        db.collection("chat")
            .whereArrayContains("member", userName)
            .orderBy("sendTime", Query.Direction.DESCENDING)
            .limit(20*count).get()
            .addOnCompleteListener(){ task ->
                if (task.isSuccessful) {
                    val fetchedMessages = task.result?.documents?.mapNotNull { document ->
                        val chat = document.toObject(Chat::class.java)
                        chat?.let {
                            if (it.member.contains(friendUserName)) {
                                Message(it.content, it.senderUsername == userName, it.sendTime)
                            } else {
                                null
                            }
                        }
                    }?.filterNot { newMessage ->
                        messages.any { data ->
                            data.content == newMessage.content &&
                                    data.sentByUser == newMessage.sentByUser &&
                                    data.time == newMessage.time
                        }
                    } ?: emptyList()
                    messages = fetchedMessages.reversed() + messages
                    oldVisibleItemIdx = listState.firstVisibleItemIndex + fetchedMessages.size
                    oldVisibleItemOffset = listState.firstVisibleItemScrollOffset
                    oldMsgFlag = true
                }
            }
    }
    val msgNewRefresh: () -> Unit = {
        db.collection("chat")
        .whereArrayContains("member", userName)
        .orderBy("sendTime", Query.Direction.DESCENDING)
        .limit(3).get()
        .addOnCompleteListener(){ task ->
            if (task.isSuccessful) {
                val fetchedMessages = task.result?.documents?.mapNotNull { document ->
                    val chat = document.toObject(Chat::class.java)
                    chat?.let {
                        if (it.member.contains(friendUserName)) {
                            Message(it.content, it.senderUsername == userName, it.sendTime)
                        } else {
                            null
                        }
                    }
                }?.filterNot { newMessage ->
                    messages.any { data ->
                        data.content == newMessage.content &&
                                data.sentByUser == newMessage.sentByUser &&
                                data.time == newMessage.time
                    }
                } ?: emptyList()
                messages += fetchedMessages
            }
        }
    }
    //加入新增時的監聽器
    db.collection("chat").addSnapshotListener{snapshots, e->
        if (e != null) {
            Log.e("error", e.toString())
            return@addSnapshotListener
        }
        if (snapshots != null) {
            for (docChange in snapshots.documentChanges) {
                when (docChange.type) {
                    DocumentChange.Type.ADDED -> {
                        msgNewRefresh()
                    }
                    DocumentChange.Type.MODIFIED -> {
                        //Nothing
                    }
                    DocumentChange.Type.REMOVED -> {
                        //Nothing
                    }
                }
            }
        }
    }
    LaunchedEffect(oldMsgFlag) {
        if(oldMsgFlag){
            listState.scrollToItem(oldVisibleItemIdx, oldVisibleItemOffset)
            oldMsgFlag = false
        }
    }
    LaunchedEffect(newMsgFlag) {
        if(newMsgFlag) {
            listState.animateScrollToItem(messages.size - 1)
            newMsgFlag = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Chat with $friend_nickName") },
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
                    state = listState,
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
                                //輸入訊息
                                db.collection("chat")
                                    .add(Chat(
                                        messageText.text,
                                        listOf(userName, friendUserName),
                                        Timestamp.now(),
                                        userName,
                                        "text"
                                    ))
                                newMsgFlag = true
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
    val isScrolledToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
        }
    }

    LaunchedEffect(isScrolledToTop) {
        if (isScrolledToTop) {
            count+=1
            msgOldRefresh()
        }
    }
}
