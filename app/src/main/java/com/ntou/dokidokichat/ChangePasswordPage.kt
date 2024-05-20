package com.ntou.dokidokichat

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import java.security.MessageDigest

class ChangePasswordPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val UserName = intent.getStringExtra(MainActivity.KEY_USER_NAME)
            ChangePasswordScreen(this, UserName)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(activity: Activity, UserName: String?) {
    val userName = UserName ?: "StarStar415"
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
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
//            Text(text = userName)
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

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password", fontSize = 15.sp) },
                visualTransformation = if (showNewPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showNewPassword = !showNewPassword }) {
                        Icon(
                            imageVector = if (showNewPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
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
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password", fontSize = 15.sp) },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                        Icon(
                            imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
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
                    if (confirmPassword != newPassword) {
                        Toast.makeText(context, "新密碼兩次輸入不同", Toast.LENGTH_SHORT).show()
                    }
                    else if (isAnyTextFieldEmpty(currentPassword,newPassword,confirmPassword)) {
                        Toast.makeText(
                            context,
                            "欄位不得為空!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else {
                        // 確認舊密碼和使用者名稱
                        val currentUser = userName
                        if (currentUser != null) {
                            // 查詢使用者
                            db.collection("user")
                                .whereEqualTo("username", userName)
                                .whereEqualTo(
                                    "password",
                                    hashPassword(currentPassword)
                                ) // 檢查舊密碼是否正確
                                .get()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // 如果舊密碼驗證成功，更新密碼
                                        val userDocument = task.result?.documents?.firstOrNull()
                                        userDocument?.let {
                                            // 更新密碼欄位
                                            db.collection("user")
                                                .document(it.id)
                                                .update("password", hashPassword(newPassword))
                                                .addOnSuccessListener {
                                                    // 密碼更新成功
                                                    Toast.makeText(
                                                        context,
                                                        "密碼已更新",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                .addOnFailureListener { exception ->
                                                    // 密碼更新失敗
                                                    Toast.makeText(
                                                        context,
                                                        "密碼更新失敗：$exception",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                        } ?: run {
                                            // 舊密碼輸入錯誤
                                            Toast.makeText(
                                                context,
                                                "舊密碼輸入錯誤",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } else {
                                        // 舊密碼驗證失敗
                                        Toast.makeText(
                                            context,
                                            "舊密碼驗證失敗",
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


fun isAnyTextFieldEmpty(currentPassword:String,newPassword:String,confirmPassword:String): Boolean {
    return currentPassword.length < 1 || newPassword.length < 1 || confirmPassword.length < 1
}
