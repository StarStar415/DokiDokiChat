package com.ntou.dokidokichat

import android.app.Activity
import android.content.Context
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

class SetIDPage : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val UserName = intent.getStringExtra(MainActivity.KEY_USER_NAME)
            SetIDScreen(this, UserName)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetIDScreen(activity: Activity, UserName: String?) {
    val userName = UserName ?: "StarStar415"
    var currentPassword by remember { mutableStateOf("") }
    var newID by remember { mutableStateOf("") }
    var confirmID by remember { mutableStateOf("") }
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewID by remember { mutableStateOf(true) }
    var showConfirmID by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.set_id)) },
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
                label = { Text(stringResource(id = R.string.current_password), fontSize = 15.sp) },
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
                value = newID,
                onValueChange = { newID = it },
                label = { Text(stringResource(id = R.string.new_id) , fontSize = 15.sp) },
                visualTransformation = if (showNewID) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showNewID = !showNewID }) {
                        Icon(
                            imageVector = if (showNewID) Icons.Default.Visibility else Icons.Default.VisibilityOff,
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
                value = confirmID,
                onValueChange = { confirmID = it },
                label = { Text(stringResource(id = R.string.confirm_new_id), fontSize = 15.sp) },
                visualTransformation = if (showConfirmID) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showConfirmID = !showConfirmID }) {
                        Icon(
                            imageVector = if (showConfirmID) Icons.Default.Visibility else Icons.Default.VisibilityOff,
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
                    if (confirmID != newID) {
                        Toast.makeText(context, "新電子郵件信箱兩次輸入不同", Toast.LENGTH_SHORT).show()
                    }
                    else if (isAnyTextFieldEmpty(currentPassword,newID,confirmID)) {
                        Toast.makeText(
                            context,
                            "欄位不得為空!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else {
                        // 確認密碼和使用者名稱
                        isIDExists(newID, context) { exists ->
                            if (exists) {
                                Toast.makeText(
                                    context,
                                    "ID 已被使用過 請換一個",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                val currentUser = userName
                                if (currentUser != null) {
                                    // 查詢使用者
                                    db.collection("user")
                                        .whereEqualTo("username", userName)
                                        .whereEqualTo(
                                            "password",
                                            hashPassword(currentPassword)
                                        ) // 檢查舊信箱是否正確
                                        .get()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val userDocument = task.result?.documents?.firstOrNull()
                                                userDocument?.let {
                                                    db.collection("user")
                                                        .document(it.id)
                                                        .update("userID", newID)
                                                        .addOnSuccessListener {
                                                            // ID更新成功
                                                            Toast.makeText(
                                                                context,
                                                                "ID已更新",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                        .addOnFailureListener { exception ->
                                                            // ID更新失敗
                                                            Toast.makeText(
                                                                context,
                                                                "ID更新失敗：$exception",
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
                                                    "舊密碼輸入錯誤",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }// ID不存在，執行相應的操作
                            }
                        }

                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48FB1)),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.set_new_id))
            }
        }
    }
}

fun isIDExists(newID: String, context: Context, callback: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val query = db.collection("user").whereEqualTo("userID", newID)
    query.get().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val documents = task.result?.documents
            if (documents != null && documents.isNotEmpty()) {
                // 如果存在相同的ID，返回true
                callback(true)
                return@addOnCompleteListener
            }
        }
        // 如果沒有相同的ID，返回false
        callback(false)
        return@addOnCompleteListener
    }.addOnFailureListener { exception ->
        // 錯誤處理，如果查詢失敗
        Toast.makeText(
            context,
            "查詢ID失敗：$exception",
            Toast.LENGTH_SHORT
        ).show()
        // 返回false
        callback(false)
    }
}

