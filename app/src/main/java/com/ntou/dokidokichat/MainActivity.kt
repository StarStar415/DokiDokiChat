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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
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
import kotlinx.coroutines.delay
import perfetto.protos.TraceAnalysisStats
import kotlin.concurrent.thread
import kotlin.concurrent.timer
import java.security.MessageDigest
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.Source


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
    var registerSuccess by remember { mutableStateOf(false) }
    var usernameTaken by remember { mutableStateOf(false) }
    var gmailTaken by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

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
                    text = stringResource(id = R.string.appTitle),
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
                            text = stringResource(id = R.string.Username),
                            fontSize = 15.sp
                        )
                    },

                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth(),
                    singleLine = true
                )
                // 信箱欄位
                if (showGmailField) {
                    // Gmail field
                    OutlinedTextField(
                        value = gmail,
                        onValueChange = { gmail = it },
                        label = {
                            Text(
                                text = stringResource(id = R.string.Email),
                                fontSize = 15.sp
                            )
                        },
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .fillMaxWidth(),
                        singleLine = true
                    )
                }

                // 密碼欄位
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(
                            text = stringResource(id = R.string.Password),
                            fontSize = 15.sp
                        )
                    },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth(),
                    visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        TextButton(onClick = { passwordVisibility = !passwordVisibility }) {
                            Icon(
                                imageVector = if (passwordVisibility) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    singleLine = true
                )

                // 登入失敗提示
                if(!loginSuccess) {
                    Text(
                        text = stringResource(id = R.string.loginFailed),
                        color = Color.Red
                    )
                }

                // username重複提示
                if(usernameTaken) {
                    Text(
                        text = stringResource(id = R.string.UsernameUse),
                        color = Color.Red
                    )
                }

                // gmail重複提示
                if(gmailTaken) {
                    Text(
                        text = stringResource(id = R.string.EmailUse),
                        color = Color.Red
                    )
                }

                if(registerSuccess){
                    Text(
                        text = stringResource(id = R.string.RegisiterSuccess),
                        color = Color.Red
                    )
                }
                // 登入|註冊按鈕
                Button(
                    onClick = {
                        isLoading = true
                        if (!showGmailField) {
                            db.collection("user")
                                .whereEqualTo("username", username)
                                .whereEqualTo("password", hashPassword(password))
                                .get(Source.SERVER).addOnCompleteListener {task ->
                                    isLoading = false
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
                            isLoading = true
                            registerUser(gmail, username, password,isLoading) { isUsernameTaken, isGmailTaken, isRegisterSuccess, isLoadingFalse->
                                usernameTaken = isUsernameTaken
                                gmailTaken = isGmailTaken
                                registerSuccess = isRegisterSuccess
                                isLoading = isLoadingFalse
                            }
                        }

                    },

//                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E2F49)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF48FB1)),
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            if (showGmailField)  stringResource(id = R.string.signUp)
                            else  stringResource(id = R.string.login),
                            color = Color.White
                        )
                    }
                }


                Row {
                    // 註冊
                    TextButton(
                        onClick = {
                            showGmailField = !showGmailField
                            loginSuccess = true
                            gmailTaken = false
                            usernameTaken = false
                            registerSuccess = false
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(if (showGmailField) stringResource(id = R.string.signIn) else stringResource(id = R.string.signUp))
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
        .get(Source.SERVER)
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
        .get(Source.SERVER)
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
    isLoading: Boolean,
    onResult: (Boolean, Boolean,Boolean,Boolean) -> Unit
) {
    isUsernameTaken(username) { isUsernameTaken ->
        if (isUsernameTaken) {
            onResult(true, false, false, false)
            return@isUsernameTaken
        }

        isGmailTaken(gmail) { isGmailTaken ->
            if (isGmailTaken) {
                onResult(false, true, false, false)
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
                    onResult(false, false, true, false) // 註冊成功
                }
                .addOnFailureListener { e ->
                    Log.e("signup", "Fail", e)
                    onResult(false, false, false, false) // 註冊失敗
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

