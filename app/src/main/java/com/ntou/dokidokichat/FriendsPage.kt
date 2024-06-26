package com.ntou.dokidokichat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.ntou.dokidokichat.data.model.Friend
import com.ntou.dokidokichat.data.model.User
import java.time.format.TextStyle

class FriendsPage : ComponentActivity() {

    companion object {
        val FRIEND_USERNAME: String = "FRIEND_USERNAME"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val UserName = intent.getStringExtra(MainActivity.KEY_USER_NAME)
            val PassWord = intent.getStringExtra(MainActivity.KEY_PASSWORD)

            ShowUserChatScreen(this,UserName,PassWord)
        }
    }
}

// 主畫面 有個人檔案,聊天列表和設定
@Composable
fun ShowUserChatScreen(activity: Activity, UserName: String?, PassWord: String?) {
    val selectedTab = remember { mutableStateOf(Tab.Profile) }

    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            when (selectedTab.value) {
                Tab.Profile -> UserProfileScreen(selectedTab, UserName)
                Tab.ChatList -> ChatListScreen(selectedTab, UserName)
                Tab.SettingList -> SettingListScreen(selectedTab, UserName,activity)
            }
            BottomNavigationScreen(selectedTab)
        }
    }
}

// 底部選單
@Composable
fun BottomNavigationScreen(selectedTab: MutableState<Tab>) {
    BottomNavigation(
        selectedTab = selectedTab.value,
        onTabSelected = { selectedTab.value = it }
    )
}

// 個人檔案 顯示好友名單和搜尋（要可以改好友暱稱）
@Composable
fun UserProfileScreen(selectedTab: MutableState<Tab>, userName: String?) {
    val userName = userName ?: "StarStar415"
    val db = FirebaseFirestore.getInstance()
    var friendsList: List<Friend> = emptyList()
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var filteredFriendsList by remember { mutableStateOf(friendsList) }
    var showDialog by remember { mutableStateOf(false) }
    var addFriendQuery by remember { mutableStateOf(TextFieldValue("")) }
    var addFriendResult by remember { mutableStateOf<User?>(null) }
    var userDisplayName by remember { mutableStateOf("User") }
    var editNameDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf(TextFieldValue("")) }
    var editFriendDialog by remember { mutableStateOf<Friend?>(null) }
    var newFriendNickname by remember { mutableStateOf(TextFieldValue("")) }
    val context = LocalContext.current

    val onSearch: () -> Unit = {
        val query = searchQuery.text
        filteredFriendsList = if (query.isEmpty()) {
            friendsList
        } else {
            friendsList.filter { it.nickname.contains(query, ignoreCase = true) }
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
                onSearch()
            }
    }

    //加入資料更改的監聽器
    db.collection("user").addSnapshotListener {snapshots, e->
        if (e != null) {
            Log.e("error", e.toString())
            return@addSnapshotListener
        }
        if (snapshots != null) {
            for (docChange in snapshots.documentChanges) {
                when (docChange.type) {
                    DocumentChange.Type.ADDED -> {
                        //Nothing
                    }
                    DocumentChange.Type.MODIFIED -> {
                        addFriendRefresh()
                    }
                    DocumentChange.Type.REMOVED -> {
                        //Nothing
                    }
                }
            }
        }
    }

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
    addFriendRefresh()

    val updateNameRefresh: () -> Unit = {
        db.collection("user").whereEqualTo("username", userName)
            .get(Source.SERVER)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result.documents[0].toObject(User::class.java)
                    userDisplayName = user?.name ?: userName ?: "User"
                    onSearch()
                }
            }
    }

    updateNameRefresh()

    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxSize()
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // 個人檔案的圖片
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .border(2.dp, color = Color(0xFFFFD9EC), CircleShape)
                        .background(color = Color(0xFFFFD9EC), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile Picture",
                        tint = Color.White,
                        modifier = Modifier
                            .size(90.dp)
                            .align(Alignment.Center)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Username
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = userDisplayName,
                        fontSize = 25.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    IconButton(onClick = { editNameDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Name")
                    }
                }


                // Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onSearch()
                        },

                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 20.sp
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp)
                            .background(
                                color = Color(0xFFFFD9EC),
                                shape = MaterialTheme.shapes.small
                            )
                            .padding(8.dp)
                        ,
                        singleLine = true
                    )
                    IconButton(onClick = onSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }

                // Friends List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .weight(1f)
                ) {
                    items(filteredFriendsList) { friend ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    editFriendDialog = friend
                                    newFriendNickname = TextFieldValue(friend.nickname)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = friend.nickname, fontSize = 25.sp, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
                // Bottom Navigation
                BottomNavigationScreen(selectedTab)
            }
            // Add Friend Button
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = Color(0xFFFCC2DF),
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp, end = 30.dp)
                    .size(75.dp),
                shape = CircleShape,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "Add Friend",
                    modifier = Modifier.size(50.dp)
                )
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(stringResource(id = R.string.add_friend)) },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = addFriendQuery,
                                    onValueChange = { addFriendQuery = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(8.dp)
                                    ,
                                    singleLine = true
                                )

                                IconButton(onClick = onAddFriendSearch) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            }

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
                                                            Toast.makeText(context, context.getString(R.string.add_friends_success), Toast.LENGTH_SHORT).show()
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
                                                    res!!.friends += Friend(50,tmp.name,tmp.userID,tmp.username)
                                                    val userRef = db.collection("user").document(task.result.documents[0].id)
                                                    userRef.update("friends", res!!.friends)
                                                        .addOnSuccessListener {
                                                            Log.d("adding", "success")
                                                        }
                                                        .addOnFailureListener { e ->
                                                            Log.e("error", "Friend error adding user")
                                                        }
                                                }
                                            showDialog = false
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
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { showDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48FB1))
                        ) {
                            Text(stringResource(id = R.string.cancel))
                        }
                    }
                )
            }

            // Edit Name Dialog
            if (editNameDialog) {
                AlertDialog(
                    onDismissRequest = { editNameDialog = false },
                    title = { Text(stringResource(id = R.string.edit_name)) },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                placeholder = { Text(stringResource(id = R.string.newName)) },
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(onClick = {
                            val newNameStr = newName.text
                            db.collection("user").whereEqualTo("username", userName)
                                .get(Source.SERVER)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val userDoc = task.result.documents[0]
                                        val userRef = db.collection("user").document(userDoc.id)
                                        userRef.update("name", newNameStr)
                                            .addOnSuccessListener {
                                                userDisplayName = newNameStr
                                                Toast.makeText(context, context.getString(R.string.update_name), Toast.LENGTH_SHORT).show()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("error", "Error updating name", e)
                                            }
                                    }
                                }

                            editNameDialog = false
                            updateNameRefresh()
                        },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48FB1)),
                        ) {
                            Text(stringResource(id = R.string.save))
                        }
                    }
                )
            }

            if (editFriendDialog != null) {
                AlertDialog(
                    onDismissRequest = { editFriendDialog = null },
                    title = {
                        Text(
                            text = stringResource(id = R.string.edit_friend_nickname)
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            BasicTextField(
                                value = newFriendNickname,
                                onValueChange = { newFriendNickname = it },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 20.sp
                                ),
                                modifier = Modifier
                                    .background(
                                        color = Color(0xFFFFD9EC),
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(8.dp)
                                    .fillMaxWidth(),
                                singleLine = true
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val friendToUpdate = editFriendDialog
                                if (friendToUpdate != null) {
                                    db.collection("user").whereEqualTo("username", userName)
                                        .get(Source.SERVER)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val userDoc = task.result.documents[0]
                                                val user = userDoc.toObject(User::class.java)
                                                if (user != null) {
                                                    val updatedFriends = user.friends.map { friend ->
                                                        if (friend.username == friendToUpdate.username) {
                                                            friend.copy(nickname = newFriendNickname.text)
                                                        } else {
                                                            friend
                                                        }
                                                    }
                                                    userDoc.reference.update("friends", updatedFriends)
                                                }
                                            }
                                            editFriendDialog = null
                                        }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48FB1)),
                        ) {
                            Text(stringResource(id = R.string.save))
                        }
                    }
                )
            }

        }
    }
}


@Composable
fun ChatListScreen(selectedTab: MutableState<Tab>, userName: String?) {
    var friendsList: List<Friend> by remember{ mutableStateOf(emptyList())}
    val db = FirebaseFirestore.getInstance()

    val addFriendRefresh: () -> Unit = {
        db.collection("user").whereEqualTo("username", userName)
            .get(Source.SERVER)
            .addOnCompleteListener { task ->
                friendsList = try {
                    task.result.documents[0].toObject(User::class.java)?.friends ?: emptyList()
                } catch(e: Exception) {
                    emptyList()
                }.sortedWith(compareByDescending<Friend>{it.favor}.thenBy{it.nickname})
            }
    }


    addFriendRefresh()
    val context = LocalContext.current

    db.collection("user").addSnapshotListener {snapshots, e->
        if (e != null) {
            Log.e("error", e.toString())
            return@addSnapshotListener
        }
        if (snapshots != null) {
            for (docChange in snapshots.documentChanges) {
                when (docChange.type) {
                    DocumentChange.Type.ADDED -> {
                        //Nothing
                    }
                    DocumentChange.Type.MODIFIED -> {
                        addFriendRefresh()
                    }
                    DocumentChange.Type.REMOVED -> {
                        //Nothing
                    }
                }
            }
        }
    }

    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top "Chats" text
            Text(
                text = stringResource(id = R.string.chats),
                fontSize = 30.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            // Friends list
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(friendsList) { friend ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .clickable {
                                clickButtonToChat(context, userName, friend.username)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(
                                    color = getFavorColor(friend.favor, 100),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = friend.favor.toString(),
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = friend.nickname,
                            fontSize = 18.sp
                        )
                    }
                }
            }

            // Bottom navigation
            BottomNavigationScreen(selectedTab)
        }
    }
}

fun getFavorColor(favor: Int, maxFavor: Int): Color {
    val startColor = Color(0xFF7E2F49)
    val endColor = Color(0xFFFFC0CB)
    val fraction = favor.toFloat() / maxFavor
    return lerp(startColor, endColor, fraction)
}

fun clickButtonToChat(context: Context, userName: String?, friendName: String) {
    val intent = Intent()
    intent.setClassName(context,
        "com.ntou.dokidokichat.ChatPage")
    intent.putExtra(MainActivity.KEY_USER_NAME, userName)
    intent.putExtra(FriendsPage.FRIEND_USERNAME, friendName)

    context.startActivity(intent)
}

@Composable
fun SettingListScreen(selectedTab: MutableState<Tab>, userName: String?,activity: Activity) {
    var logoutDialog by remember { mutableStateOf(false) }
    var selectedMenuItem by remember { mutableStateOf<String?>(null) }

    Surface(
        color = Color.White,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {


            Text(
                text = stringResource(id = R.string.setting),
                fontSize = 30.sp,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val menuItems = listOf(
                    R.string.profile,
                    R.string.change_password,
                    R.string.change_email,
                    R.string.set_id,
                    R.string.add_friend,
                    R.string.friends,
                    R.string.log_out,
                    R.string.about
                )


                items(menuItems) {itemResId ->
                    val item = stringResource(id = itemResId)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedMenuItem = item ?: "StarStar415"
                                handleMenuItemClick(
                                    itemResId,
                                    activity,
                                    userName,
                                    { selectedTab.value = it },
                                    { logoutDialog = true })
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(id = itemResId),
                                fontSize = 25.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "go to another page"
                            )
                        }
                    }
                }

            }
            BottomNavigationScreen(selectedTab)

            if (logoutDialog) {
                AlertDialog(
                    onDismissRequest = { logoutDialog = false },
                    title = { Text(stringResource(id = R.string.logOut)) },
                    text = { Text(stringResource(id = R.string.logoutMes)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                logoutDialog = false
                                activity.finish()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48FB1)),
                            shape = MaterialTheme.shapes.medium,
                        ) {
                            Text(stringResource(id = R.string.yes), color = Color.White)
                        }
                    },
                    dismissButton = {
                        Button(
                            onClick = { logoutDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                            shape = MaterialTheme.shapes.medium,

                        ) {
                            Text(stringResource(id = R.string.no), color = Color.White)
                        }
                    }
                )
            }

        }
    }
}


fun handleMenuItemClick(itemResId: Int, activity: Activity, userName: String?, onTabSelected: (Tab) -> Unit, onLogout: () -> Unit) {
    when (itemResId) {
        R.string.profile -> {
            onTabSelected(Tab.Profile)
        }
        R.string.change_password -> {
            clickButtonToChangePassword(activity, userName)
        }
        R.string.change_email -> {
            clickButtonToChangeEmail(activity, userName)
        }
        R.string.set_id -> {
            clickButtonToSetID(activity, userName)
        }
        R.string.add_friend -> {
            clickButtonToAddFriends(activity, userName)
        }
        R.string.friends -> {
            clickButtonToShowFriends(activity, userName)
        }
        R.string.log_out -> {
            onLogout()
        }
        R.string.about -> {
            clickButtonToAboutPage(activity, userName)
        }

    }
}

fun clickButtonToAboutPage(context: Context, userName: String?) {
    val intent = Intent()
    intent.setClassName(context,
        "com.ntou.dokidokichat.aboutPage")
    intent.putExtra(MainActivity.KEY_USER_NAME, userName)
    context.startActivity(intent)
}
fun clickButtonToAddFriends(context: Context, userName: String?) {
    val intent = Intent()
    intent.setClassName(context,
        "com.ntou.dokidokichat.addFriendsPage")
    intent.putExtra(MainActivity.KEY_USER_NAME, userName)
    context.startActivity(intent)
}
fun clickButtonToChangePassword(context: Context, userName: String?) {
    val intent = Intent()
    intent.setClassName(context,
        "com.ntou.dokidokichat.ChangePasswordPage")
    intent.putExtra(MainActivity.KEY_USER_NAME, userName)
    context.startActivity(intent)
}

fun clickButtonToChangeEmail(context: Context, userName: String?) {
    val intent = Intent()
    intent.setClassName(context,
        "com.ntou.dokidokichat.ChangeEmailPage")
    intent.putExtra(MainActivity.KEY_USER_NAME, userName)
    context.startActivity(intent)
}

fun clickButtonToSetID(context: Context, userName: String?) {
    val intent = Intent()
    intent.setClassName(context,
        "com.ntou.dokidokichat.SetIDPage")
    intent.putExtra(MainActivity.KEY_USER_NAME, userName)
    context.startActivity(intent)
}

fun clickButtonToShowFriends(context: Context, userName: String?) {
    val intent = Intent()
    intent.setClassName(context,
        "com.ntou.dokidokichat.ShowFriendsPage")
    intent.putExtra(MainActivity.KEY_USER_NAME, userName)
    context.startActivity(intent)
}

@Composable
fun BottomNavigation(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp)
            .background(color = Color(0xFFF48FB1)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BottomNavigationItem(
            icon = Icons.Default.Person,
            text = stringResource(id = R.string.profile),
            selected = selectedTab == Tab.Profile,
            onClick = { onTabSelected(Tab.Profile) },
        )

        BottomNavigationItem(
            icon = Icons.Default.Send,
            text = stringResource(id = R.string.chatList),
            selected = selectedTab == Tab.ChatList,
            onClick = { onTabSelected(Tab.ChatList) },
        )

        BottomNavigationItem(
            icon = Icons.Default.Settings,
            text = stringResource(id = R.string.setting),
            selected = selectedTab == Tab.SettingList,
            onClick = { onTabSelected(Tab.SettingList) },
        )
    }
}

@Composable
fun BottomNavigationItem(
    icon: ImageVector,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(35.dp)
            )
            Text(
                text = text,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

enum class Tab { Profile, ChatList, SettingList }
