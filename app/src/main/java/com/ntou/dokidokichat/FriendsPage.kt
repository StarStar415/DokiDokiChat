package com.ntou.dokidokichat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

class FriendsPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShowUserChatScreen()
        }
    }
}



@Composable
fun ShowUserChatScreen() {
    var selectedTab by remember { mutableStateOf(Tab.Profile) }

    Column(
        Modifier.fillMaxSize(1f)) {
        // Content of the current tab
        when (selectedTab) {
            Tab.Profile -> UserProfileScreen()
            Tab.ChatList -> ChatListScreen()
        }

        // Bottom Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavigationItem(
                icon = Icons.Default.Person,
                text = "Profile",
                selected = selectedTab == Tab.Profile,
                onClick = { selectedTab = Tab.Profile }
            )

            BottomNavigationItem(
                icon = Icons.Default.Send,
                text = "Chat List",
                selected = selectedTab == Tab.ChatList,
                onClick = { selectedTab = Tab.ChatList }
            )
        }
    }
}

@Composable
fun UserProfileScreen() {
    // Content of the user profile screen
    Text("User Profile Screen")
}

@Composable
fun ChatListScreen() {
    // Content of the chat list screen
    Text("Chat List Screen")
}

@Composable
fun BottomNavigationItem(
    icon: ImageVector,
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null)
            Text(text)
        }
    }
}

enum class Tab { Profile, ChatList }