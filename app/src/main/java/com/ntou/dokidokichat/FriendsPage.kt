package com.ntou.dokidokichat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class FriendsPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val UserName = intent.getStringExtra( MainActivity.KEY_USER_NAME )
            val PassWord = intent.getStringExtra( MainActivity.KEY_PASSWORD )

            ShowUserChatScreen()
        }
    }
}


@Composable
fun ShowUserChatScreen() {
    val selectedTab = remember { mutableStateOf(Tab.Profile) }

    Surface(
        color = Color.White, // White background
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.weight(1f))

            when (selectedTab.value) {
                Tab.Profile -> UserProfileScreen(selectedTab)
                Tab.ChatList -> ChatListScreen(selectedTab)
                Tab.SettingList -> SettingListScreen(selectedTab)
            }

            Spacer(modifier = Modifier.weight(1f))

            BottomNavigationScreen(selectedTab)
        }
    }
}

@Composable
fun BottomNavigationScreen(selectedTab: MutableState<Tab>) {
    BottomNavigation(
        selectedTab = selectedTab.value,
        onTabSelected = { selectedTab.value = it }
    )
}


@Composable
fun UserProfileScreen(selectedTab: MutableState<Tab>) {
    Text(
        text = "User Profile Screen",
    )
}

@Composable
fun ChatListScreen(selectedTab: MutableState<Tab>) {
    Surface(
        color = Color.White ,// Pink background
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Chat List Screen",
            )
            Spacer(modifier = Modifier.weight(1f))

            BottomNavigationScreen(selectedTab)
        }
    }

}

@Composable
fun SettingListScreen(selectedTab: MutableState<Tab>) {
    Surface(
        color = Color.White ,// Pink background
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Setting List Screen",
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