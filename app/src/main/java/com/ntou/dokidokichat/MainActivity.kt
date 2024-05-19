package com.ntou.dokidokichat

import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.plus
import androidx.core.graphics.times
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.google.firebase.firestore.FirebaseFirestore
import com.ntou.dokidokichat.data.model.User
import java.security.MessageDigest
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {

    companion object {
        val KEY_USER_NAME: String = "KEY_USER_NAME"
        val KEY_PASSWORD: String = "KEY_PASSWORD"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = FirebaseFirestore.getInstance()

        setContent {
            LoginScreen(db)
        }
    }
}

@Composable
fun LoginScreen(db: FirebaseFirestore) {
    var context = LocalContext.current
    var username by remember { mutableStateOf("starstar415") }
    var password by remember { mutableStateOf("01057132star") }
    var gmail by remember { mutableStateOf("01057132@email.ntou.edu.tw") }
    var showGmailField by remember { mutableStateOf(false) }
    var loginSuccess by remember { mutableStateOf(true) }
    var usernameTaken by remember { mutableStateOf(false) }
    var gmailTaken by remember { mutableStateOf(false) }

    var passwordVisibility by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFFFFC1E0),

        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFFFD9EC))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                contentAlignment = Alignment.Center,
            ){
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(10.dp)
            ){
                // 畫愛心
                DokiDokiHeart()

                // 標題
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "DokiDoki Chat",
                    fontSize = 30.sp,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .paddingFromBaseline(
                            top = 0.dp,
                            bottom = 8.dp
                        )
                )
                // 使用者名稱欄位
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        Text(
                            "UserName",
                            fontSize = 15.sp
                        )
                    },

                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                )
                // 信箱欄位
                if (showGmailField) {
                    // Gmail field
                    OutlinedTextField(
                        value = gmail,
                        onValueChange = { gmail = it },
                        label = {
                            Text(
                                "Gmail",
                                fontSize = 15.sp
                            )
                        },
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth()
                    )
                }

                // 密碼欄位
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(
                            "Password",
                            fontSize = 15.sp
                        )
                    },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth(),
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Text(if (passwordVisibility) "Hide" else "Show")
                        }
                    }
                )

                // 登入失敗提示
                if(!loginSuccess) {
                    Text(
                        "Login failed",
                        color = Color.Red
                    )
                }

                // username重複提示
                if(usernameTaken) {
                    Text(
                        "Username is already taken",
                        color = Color.Red
                    )
                }

                // gmail重複提示
                if(gmailTaken) {
                    Text(
                        "Gmail is already registered",
                        color = Color.Red
                    )
                }
                // 登入|註冊按鈕
                Button(
                    onClick = {
                        if (!showGmailField) {
                            db.collection("user")
                                .whereEqualTo("username", username)
                                .whereEqualTo("password", hashPassword(password))
                                .get().addOnCompleteListener {task ->
                                    if (task.isSuccessful && task.result != null && task.result
                                            .documents
                                            .size > 0
                                    ) {
                                        loginSuccess = true
                                        clickButtonToChat(context, username, password)
                                    } else {
                                        loginSuccess = false
                                        Log.e("Login", "Login failed")
                                    }
                                }
                        } else {
                            registerUser(gmail, username, password) { isUsernameTaken, isGmailTaken ->
                                usernameTaken = isUsernameTaken
                                gmailTaken = isGmailTaken
                            }
                        }

                    },

                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48FB1)),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        if (showGmailField) "Sign up" else "Login",
                        color = Color.White
                    )
                }



                Row {
                    // 註冊
                    TextButton(
                        onClick = {
                            showGmailField = !showGmailField
                            loginSuccess = true
                            gmailTaken = false
                            usernameTaken = false
                        },
                        modifier = Modifier.padding(top = 8.dp) // 添加上方的外邊距
                    ) {
                        Text(if (showGmailField) "Sign in" else "Sign up")
                    }
                    TextButton(
                        onClick = { /* 忘記密碼操作 */ },
                        modifier = Modifier.padding(top = 8.dp) // 添加上方的外邊距
                    ) {
                        Text("Forgot password?")
                    }
                }
            }

            }
        }
    }
}

fun hashPassword(password: String): String {
    val bytes = password.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}


fun clickButtonToChat(context: Context, username: String, password: String) {
    val intent = Intent()
    intent.setClassName(context,
        "com.ntou.dokidokichat.FriendsPage")
    intent.putExtra(MainActivity.KEY_USER_NAME, username)
    intent.putExtra(MainActivity.KEY_PASSWORD, password)

    context.startActivity(intent)
}

fun isUsernameTaken(username: String, callback: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("user")
        .whereEqualTo("username", username)
        .get()
        .addOnSuccessListener { documents ->
            callback(!documents.isEmpty)
        }
        .addOnFailureListener { e ->
            Log.e("checkUsername", "Error checking username", e)
            callback(false)
        }
}
fun isGmailTaken(gmail: String, callback: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("user")
        .whereEqualTo("email", gmail)
        .get()
        .addOnSuccessListener { documents ->
            callback(!documents.isEmpty)
        }
        .addOnFailureListener { e ->
            Log.e("checkGmail", "Error checking gmail", e)
            callback(false)
        }
}
fun registerUser(
    gmail: String,
    username: String,
    password: String,
    onResult: (Boolean, Boolean) -> Unit
) {
    isUsernameTaken(username) { isUsernameTaken ->
        if (isUsernameTaken) {
            onResult(true, false)
            return@isUsernameTaken
        }

        isGmailTaken(gmail) { isGmailTaken ->
            if (isGmailTaken) {
                onResult(false, true)
                return@isGmailTaken
            }

            // 註冊
            val db = FirebaseFirestore.getInstance()
            db.collection("user").add(User(
                "",    // 頭貼
                gmail,           // gmail
                emptyList(),     // 好友名單
                false,
                username,       // name
                hashPassword(password), // password(hash)
                username,       // userID
                username       // username
            ))
                .addOnSuccessListener { documentReference ->
                    Log.d("signup", "Success")
                    onResult(false, false) // 註冊成功
                }
                .addOnFailureListener { e ->
                    Log.e("signup", "Fail", e)
                    onResult(false, false) // 註冊失敗
                }
        }
    }
}

@Composable
fun DokiDokiHeart(){
    fun directionVectorPointF(angleRadians: Float) =
        PointF(cos(angleRadians), sin(angleRadians))
    fun Float.toRadians() = this * PI.toFloat() / 180f

    val PointZero = PointF(0f, 0f)
    fun radialToCartesian(
        radius: Float,
        angleRadians: Float,
        center: PointF = PointZero
    ) = directionVectorPointF(angleRadians) * radius + center
    val vertices = remember {
        val radius = 1f
        val radiusSides = 0.8f
        val innerRadius = .1f
        floatArrayOf(
            radialToCartesian(radiusSides, 0f.toRadians()).x,
            radialToCartesian(radiusSides, 0f.toRadians()).y,
            radialToCartesian(radius, 90f.toRadians()).x,
            radialToCartesian(radius, 90f.toRadians()).y,
            radialToCartesian(radiusSides, 180f.toRadians()).x,
            radialToCartesian(radiusSides, 180f.toRadians()).y,
            radialToCartesian(radius, 250f.toRadians()).x,
            radialToCartesian(radius, 250f.toRadians()).y,
            radialToCartesian(innerRadius, 270f.toRadians()).x,
            radialToCartesian(innerRadius, 270f.toRadians()).y,
            radialToCartesian(radius, 290f.toRadians()).x,
            radialToCartesian(radius, 290f.toRadians()).y,
        )
    }

    val rounding = remember {
        val roundingNormal = 0.6f
        val roundingNone = 0f
        listOf(
            CornerRounding(roundingNormal),
            CornerRounding(roundingNone),
            CornerRounding(roundingNormal),
            CornerRounding(roundingNormal),
            CornerRounding(roundingNone),
            CornerRounding(roundingNormal),
        )
    }

    val polygon = remember(vertices, rounding) {
        RoundedPolygon(
            vertices = vertices,
            perVertexRounding = rounding
        )
    }

    Box(
        modifier = Modifier
            .drawWithCache {
                val roundedPolygonPath = polygon
                    .toPath()
                    .asComposePath()
                onDrawBehind {
                    scale(size.width * 0.5f, size.width * 0.5f) {
                        translate(size.width * 0.5f, size.height * 0.5f) {
                            drawPath(
                                roundedPolygonPath,
                                color = Color(0xFFFF79BC)
                            )
                        }
                    }
                }
            }
            .size(200.dp)
    )
}

