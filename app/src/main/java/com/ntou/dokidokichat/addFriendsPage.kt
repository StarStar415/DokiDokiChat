package com.ntou.dokidokichat

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.ntou.dokidokichat.data.model.Friend
import com.ntou.dokidokichat.data.model.User

class addFriendsPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val UserName = intent.getStringExtra(MainActivity.KEY_USER_NAME)
            addFriendsPage(this, UserName)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun addFriendsPage(activity: Activity, UserName: String?) {
    val userName = UserName ?: "StarStar415"
    var addFriendQuery by remember { mutableStateOf(TextFieldValue("")) }
    var addFriendResult by remember { mutableStateOf<User?>(null) }
    var friendsList: List<Friend> = emptyList()
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    val onAddFriendSearch: () -> Unit = {
        val query = addFriendQuery.text
        db.collection("user").whereEqualTo("userID", query)
            .get(Source.SERVER)
            .addOnCompleteListener {task->
                addFriendResult = try {
                    task.result.documents[0].toObject(User::class.java)
                } catch(e: Exception) {
                    null
                }
                if (addFriendResult == null) {
                    Toast.makeText(context, context.getString(R.string.user_not_found), Toast.LENGTH_SHORT).show()
                }
                else if (addFriendResult!!.username == userName) {
                    Toast.makeText(context, context.getString(R.string.cannot_add_self), Toast.LENGTH_LONG).show()
                    addFriendResult = null
                }
                else if (friendsList.any { it.username == addFriendResult!!.username }){
                    Toast.makeText(context, context.getString(R.string.already_friends), Toast.LENGTH_LONG).show()
                    addFriendResult = null
                }
            }
    }
    val addFriendRefresh: () -> Unit = {
        db.collection("user").whereEqualTo("username", userName)
            .get(Source.SERVER)
            .addOnCompleteListener { task ->
                friendsList = try {
                    task.result.documents[0].toObject(User::class.java)?.friends ?: emptyList()
                } catch(e: Exception) {
                    emptyList()
                }.sortedWith(compareBy<Friend>{it.favor}.thenBy{it.nickname})
            }
    }
    addFriendRefresh()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.add_friend)) },
                navigationIcon = {
                    IconButton(onClick = { activity.onBackPressed() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFC1E0))
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = addFriendQuery,
                    onValueChange = { addFriendQuery = it },
                    label = { Text(stringResource(id = R.string.add_friend), fontSize = 15.sp) },

                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))


                Button(
                    onClick = onAddFriendSearch,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48FB1)),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.search))
                }

                Spacer(modifier = Modifier.height(50.dp))

                addFriendResult?.let {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = it.name,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                        Button(
                            onClick = {
                                lateinit var tmp:User
                                //使用者加好友
                                db.collection("user")
                                    .whereEqualTo("username", userName)
                                    .get(Source.SERVER)
                                    .addOnCompleteListener(){task->
                                        val res = task.result.documents[0].toObject(User::class.java)
                                        tmp = res!!
                                        res.friends += Friend(50,it.name,it.userID,it.username, false)
                                        val userRef = db.collection("user").document(task.result.documents[0].id)
                                        userRef.update("friends", res.friends)
                                            .addOnSuccessListener {
                                                Toast.makeText(context, "加入成功", Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("error", "Error adding friend")
                                            }
                                    }
                                //被加好友方同步加使用者好友
                                db.collection("user")
                                    .whereEqualTo("username", it.username)
                                    .get(Source.SERVER)
                                    .addOnCompleteListener(){task->
                                        val res = task.result.documents[0].toObject(User::class.java)
                                        res!!.friends += Friend(0,tmp.name,tmp.userID,tmp.username)
                                        val userRef = db.collection("user").document(task.result.documents[0].id)
                                        userRef.update("friends", res!!.friends)
                                            .addOnSuccessListener {
                                                Log.d("adding", "success")
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("error", "Friend error adding user")
                                            }
                                    }
                                addFriendQuery = TextFieldValue("")
                                addFriendResult = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48FB1)),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(stringResource(id = R.string.add_friend))
                        }
                    }
                }
            }
        }

    }
}