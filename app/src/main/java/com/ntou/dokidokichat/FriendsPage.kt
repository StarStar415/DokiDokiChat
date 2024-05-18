package com.ntou.dokidokichat

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import java.time.format.TextStyle

class FriendsPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val UserName = intent.getStringExtra(MainActivity.KEY_USER_NAME)
            val PassWord = intent.getStringExtra(MainActivity.KEY_PASSWORD)

            ShowUserChatScreen(UserName,PassWord)
        }
    }
}

// 主畫面 有個人檔案,聊天列表和設定
@Composable
fun ShowUserChatScreen(UserName: String?,PassWord: String?) {
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
                Tab.SettingList -> SettingListScreen(selectedTab, UserName)
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
                    .padding(bottom = 100.dp, end = 30.dp),
                shape = CircleShape,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Friend")
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
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Chat List Screen",
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
            BottomNavigationScreen(selectedTab)
        }
    }
}

@Composable
fun SettingListScreen(selectedTab: MutableState<Tab>, userName: String?) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Setting List Screen",
                fontSize = 24.sp,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
            BottomNavigationScreen(selectedTab)
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
