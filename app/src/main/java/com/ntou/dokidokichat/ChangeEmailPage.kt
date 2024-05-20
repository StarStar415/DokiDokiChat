package com.ntou.dokidokichat

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

class ChangeEmailPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val UserName = intent.getStringExtra(MainActivity.KEY_USER_NAME)
            ChangeEmailScreen(this, UserName)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeEmailScreen(activity: Activity, UserName: String?) {
    val userName = UserName ?: "StarStar415"
    var currentEmail by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var newEmail by remember { mutableStateOf("") }
    var confirmEmail by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showCurrentEmail by remember { mutableStateOf(false) }
    var showNewEmail by remember { mutableStateOf(false) }
    var showConfirmEmail by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Email") },
                navigationIcon = {
                    IconButton(onClick = { activity.onBackPressed() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFFFC1E0))
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = currentPassword,
                onValueChange = { currentPassword = it },
                label = { Text("Current Password", fontSize = 15.sp) },
                visualTransformation = if (showCurrentPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                        Icon(
                            imageVector = if (showCurrentPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            )
            OutlinedTextField(
                value = currentEmail,
                onValueChange = { currentEmail = it },
                label = { Text("Current Email", fontSize = 15.sp) },
                visualTransformation = if (showCurrentEmail) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showCurrentEmail = !showCurrentEmail }) {
                        Icon(
                            imageVector = if (showCurrentEmail) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newEmail,
                onValueChange = { newEmail = it },
                label = { Text("New Email", fontSize = 15.sp) },
                visualTransformation = if (showNewEmail) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showNewEmail = !showNewEmail }) {
                        Icon(
                            imageVector = if (showNewEmail) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmEmail,
                onValueChange = { confirmEmail = it },
                label = { Text("Confirm New Email", fontSize = 15.sp) },
                visualTransformation = if (showConfirmEmail) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmEmail = !showConfirmEmail }) {
                        Icon(
                            imageVector = if (showConfirmEmail) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 確認密碼是否相同，如果相同則對密碼進行更新並顯示更新結果
            Button(
                onClick = {
                    if (confirmEmail != newEmail) {
                        Toast.makeText(context, "新電子郵件信箱兩次輸入不同", Toast.LENGTH_SHORT).show()
                    }
                    else if (isAnyTextFieldEmpty(currentPassword,currentEmail,newEmail,confirmEmail)) {
                        Toast.makeText(
                            context,
                            "欄位不得為空!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else {
                        // 確認舊信箱和使用者名稱
                        val currentUser = userName
                        if (currentUser != null) {
                            // 查詢使用者
                            db.collection("user")
                                .whereEqualTo("username", userName)
                                .whereEqualTo(
                                    "password",
                                    hashPassword(currentPassword)
                                ) // 檢查舊密碼是否正確
                                .whereEqualTo(
                                    "email",
                                    currentEmail
                                ) // 檢查舊信箱是否正確
                                .get()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // 如果舊密碼驗證成功，更新密碼
                                        val userDocument = task.result?.documents?.firstOrNull()
                                        userDocument?.let {
                                            // 更新密碼欄位
                                            db.collection("user")
                                                .document(it.id)
                                                .update("email", newEmail)
                                                .addOnSuccessListener {
                                                    // 密碼更新成功
                                                    Toast.makeText(
                                                        context,
                                                        "電子郵件已更新",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                .addOnFailureListener { exception ->
                                                    // 密碼更新失敗
                                                    Toast.makeText(
                                                        context,
                                                        "電子郵件更新失敗：$exception",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        } ?: run {
                                            // 舊密碼輸入錯誤
                                            Toast.makeText(
                                                context,
                                                "舊密碼或是信箱輸入錯誤",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } else {
                                        // 舊密碼驗證失敗
                                        Toast.makeText(
                                            context,
                                            "舊密碼或是信箱輸入錯誤",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48FB1)),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Change Password")
            }
        }
    }
}

fun isAnyTextFieldEmpty(currentPassword: String, currentEmail:String,newEmail:String,confirmEmail:String): Boolean {
    return currentPassword.length < 1 ||currentEmail.length < 1 || newEmail.length < 1 || confirmEmail.length < 1
}
