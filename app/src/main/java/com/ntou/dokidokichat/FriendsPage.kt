package com.ntou.dokidokichat

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import java.time.format.TextStyle

class FriendsPage : ComponentActivity() {

    companion object {
        val FRIEND_NAME: String = "FRIEND_NAME"
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
        color = Color.White, // White background
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

@Composable
fun UserProfileScreen(selectedTab: MutableState<Tab>, userName: String?) {
    val userName = userName ?: "StarStar415"
    val friendsList = listOf(
        "01057132", "01057132", "01057132", "01057132", "01057120", "01057122",
        "01057122", "01057115", "01057112", "01057124", "star", "starstar",
        "starstar_0415", "StarStar415"
    )
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var filteredFriendsList by remember { mutableStateOf(friendsList) }
    var showDialog by remember { mutableStateOf(false) }
    var addFriendQuery by remember { mutableStateOf(TextFieldValue("")) }
    var addFriendResult by remember { mutableStateOf<String?>(null) }

    val onSearch: () -> Unit = {
        val query = searchQuery.text
        filteredFriendsList = if (query.isEmpty()) {
            friendsList
        } else {
            friendsList.filter { it.contains(query, ignoreCase = true) }
        }
    }

    val onAddFriendSearch: () -> Unit = {
        val query = addFriendQuery.text
        addFriendResult = friendsList.find { it == query }
    }

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
                Text(
                    text = userName,
                    fontSize = 25.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

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
                            .background(color = Color(0xFFFFD9EC), shape = MaterialTheme.shapes.small)
                            .padding(8.dp)
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
                        Text(text = friend, fontSize = 25.sp, modifier = Modifier.padding(8.dp))
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
                    title = { Text("Add Friend") },
                    text = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BasicTextField(
                                    value = addFriendQuery,
                                    onValueChange = { addFriendQuery = it },
                                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            color = Color(0xFFFFD9EC),
                                            shape = MaterialTheme.shapes.small
                                        )
                                        .padding(8.dp)
                                )
                                IconButton(onClick = onAddFriendSearch) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            }

                            addFriendResult?.let {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = it,
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(top = 16.dp)
                                    )
                                    Button(
                                        onClick = { /* Handle add friend action */ },
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text("加入好友")
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("close")
                        }
                    }
                )
            }
        }
    }
}




@Composable
fun ChatListScreen(selectedTab: MutableState<Tab>, userName: String?) {
    val friendsList = listOf(
        "Alice", "Bob", "Charlie", "David", "Eve", "Frank", "Grace", "Hannah","star","starstar415"
    )
    var context = LocalContext.current

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
                text = "Chats",
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
                                clickButtonToChat(context, friend)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(color = Color(0xFFFFD9EC), shape = CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Profile Picture",
                                tint = Color.White,
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.Center)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = friend,
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

fun clickButtonToChat(context: Context, friendName: String) {
    val intent = Intent()
    intent.setClassName(context,
        "com.ntou.dokidokichat.ChatPage")
    intent.putExtra(FriendsPage.FRIEND_NAME, friendName)

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
                text = "Setting",
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
                    "Profile",
                    "Change Password",
                    "Change Email",
                    "Edit Profile",
                    "Set ID",
                    "Add Friend",
                    "Friends",
                    "Daily Horoscope",
                    "Log Out"
                )


                items(menuItems) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedMenuItem = item
                                handleMenuItemClick(item, { selectedTab.value = it }, { logoutDialog = true })
                            }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = item,
                                fontSize = 25.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = ">",
                                fontSize = 25.sp
                            )
                        }
                    }
                }

            }
            // debug 測試現在選的東西
            selectedMenuItem?.let { menuItem ->
                Text(
                    text = "Selected: $menuItem",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(16.dp)
                )
            }
            BottomNavigationScreen(selectedTab)

            if (logoutDialog) {
                AlertDialog(
                    onDismissRequest = { logoutDialog = false },
                    title = { Text("Log Out") },
                    text = { Text("Are you sure you want to log out?") },
                    confirmButton = {
                        Button(onClick = {
                            logoutDialog = false
                            activity.finish()
                        }) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { logoutDialog = false }) {
                            Text("No")
                        }
                    }
                )
            }
        }
    }
}

fun handleMenuItemClick(item: String, onTabSelected: (Tab) -> Unit, onLogout: () -> Unit) {
    when (item) {
        "Profile" -> {
            onTabSelected(Tab.Profile)
        }
        "Change Password" -> {

        }
        "Edit Profile" -> {

        }
        "Set ID" -> {

        }
        "Add Friend" -> {

        }
        "Friends" -> {

        }
        "Log Out" -> {
            onLogout()
        }
    }
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
            text = "Profile",
            selected = selectedTab == Tab.Profile,
            onClick = { onTabSelected(Tab.Profile) },
        )

        BottomNavigationItem(
            icon = Icons.Default.Send,
            text = "Chat List",
            selected = selectedTab == Tab.ChatList,
            onClick = { onTabSelected(Tab.ChatList) },
        )

        BottomNavigationItem(
            icon = Icons.Default.Settings,
            text = "Setting",
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
