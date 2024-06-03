package com.ntou.dokidokichat

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.ntou.dokidokichat.data.model.Chat
import com.ntou.dokidokichat.data.model.User
import kotlinx.coroutines.delay

lateinit var member_string: String



class ChatPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userName = intent.getStringExtra(MainActivity.KEY_USER_NAME)
            val friendUserName = intent.getStringExtra(FriendsPage.FRIEND_USERNAME)
            val tmp = listOf(userName!!, friendUserName!!).sorted()
            member_string = ""
            for(t in tmp) {
                member_string += "$t/"
            }
            member_string = member_string.substring(0, member_string.lastIndex)
            ShowChatScreen(userName, friendUserName, onBackPressed = { finish() }, this)
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
fun ShowChatScreen(userName: String, friendUserName: String, onBackPressed: () -> Unit, context: Context) {
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
    var bottomFlag by remember { mutableStateOf(false)}
    var noInternet by remember { mutableStateOf(false)}
    var preCount by remember { mutableLongStateOf(0) }
    var count by remember { mutableLongStateOf(1)}
    var oldVisibleItemIdx by remember { mutableIntStateOf(1) }
    var oldVisibleItemOffset by remember { mutableIntStateOf(1) }
    lateinit var friendData: User
    var friend_nickName by remember{ mutableStateOf("")}
    var friend_favor by remember { mutableIntStateOf(0) }

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    LaunchedEffect(true) {
        while(true) {
            noInternet = connectivityManager.activeNetwork == null
            delay(1000)
        }
    }
//    db.collection("user").whereEqualTo("username", friendUserName).get(Source.SERVER)
//        .addOnCompleteListener(){task->
//            if(task.isSuccessful) {
//                friendData = task.result.documents[0].toObject(User::class.java)!!
//                friend_nickName = friendData.name
//
//            }
//            else {
//                Toast.makeText(context, "尚未連接網路", Toast.LENGTH_SHORT).show()
//            }
//        }
    db.collection("user")
        .whereEqualTo("username", userName) // 查询当前用户
        .get(Source.SERVER)
        .addOnSuccessListener { userSnapshot ->
            if (!userSnapshot.isEmpty) {
                val userDocument = userSnapshot.documents[0]
                val friendsList = userDocument.toObject(User::class.java)?.friends ?: emptyList()

                friendsList.forEach { friend ->
                    if(friend.username == friendUserName) {
                        friend_nickName = friend.nickname
                        friend_favor = friend.favor
                    }
                }
            }
        }
    val msgOldRefresh: () -> Unit = {
        db.collection("chat")
            .whereEqualTo("member", member_string)
            .orderBy("sendTime", Query.Direction.DESCENDING)
            .limit(20*count).get(Source.SERVER)
            .addOnCompleteListener(){ task ->
                if (task.isSuccessful) {
                    val fetchedMessages = task.result?.documents?.mapNotNull { document ->
                        val chat = document.toObject(Chat::class.java)
                        chat?.let {
                            Message(it.content, it.senderUsername == userName, it.sendTime)
                        }
                    }?.filterNot { newMessage ->
                        messages.any { data ->
                            data.content == newMessage.content &&
                                    data.sentByUser == newMessage.sentByUser &&
                                    data.time == newMessage.time
                        }
                    } ?: emptyList()
                    preCount = count
                    if(fetchedMessages != emptyList<Message>()) {
                        messages = fetchedMessages.reversed() + messages
                        oldVisibleItemIdx = listState.firstVisibleItemIndex + fetchedMessages.size
                        oldVisibleItemOffset = listState.firstVisibleItemScrollOffset
                        oldMsgFlag = true
                        count += 1
                    }
                }
            }
    }
    LaunchedEffect(noInternet) {
        if(!noInternet) {
            msgOldRefresh()
        }
        while(noInternet) {
            delay(1000)
        }
    }

    val msgNewRefresh: () -> Unit = {
        db.collection("chat")
        .whereEqualTo("member", member_string)
        .orderBy("sendTime", Query.Direction.DESCENDING)
        .limit(2).get(Source.SERVER)
        .addOnCompleteListener(){ task ->
            if (task.isSuccessful) {
                val fetchedMessages = task.result?.documents?.mapNotNull { document ->
                    val chat = document.toObject(Chat::class.java)
                    chat?.let {
                        Message(it.content, it.senderUsername == userName, it.sendTime)
                    }
                }?.filterNot { newMessage ->
                    messages.any { data ->
                        data.content == newMessage.content &&
                                data.sentByUser == newMessage.sentByUser &&
                                data.time == newMessage.time
                    }
                } ?: emptyList()
                bottomFlag = !listState.canScrollForward
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
    LaunchedEffect(messages) {
        if(newMsgFlag || bottomFlag) {
            try {
                listState.animateScrollToItem(messages.size - 1)
            } catch (e: Exception) {
                Log.d("newChat", "first msg!")
            }
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
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(getFavorColor(friend_favor,100), shape = CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$friend_favor",
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFC1E0))
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
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .background(
                                            Color(0xFFFFA4B4),
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
                                    fontSize = 16.sp,
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
                    IconButton(
                        onClick = {
                            if (messageText.text.isNotBlank()) {
                                if (!noInternet) {
                                    //輸入訊息
                                    db.collection("chat")
                                        .add(
                                            Chat(
                                                messageText.text,
                                                member_string,
                                                Timestamp.now(),
                                                userName,
                                                "text"
                                            )
                                        )
                                    newMsgFlag = true
                                    messageText = TextFieldValue("")
                                }
                            }
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        if(!noInternet) {
                            Icon(Icons.Filled.Send, contentDescription = "Send")
                        }
                        else {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
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
        if (isScrolledToTop && preCount != count && preCount != 0L) {
            msgOldRefresh()
        }
    }
}
