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
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class MainActivity : ComponentActivity() {
    object Constants {
        const val PAGE_SIZE: Int = 20
        object LoginAPI {
            const val URL: String = "http://localhost:8080/"
            const val NAME = "login_api"
        }

        object SignUpAPI {
            const val URL: String = "http://localhost:8080/"
            const val NAME = "sign_up_api"
        }
    }
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
    var username by remember { mutableStateOf("starstar") }
    var password by remember { mutableStateOf("01057132") }
    var gmail by remember { mutableStateOf("01057132@email.ntou.edu.tw") }
    var showGmailField by remember { mutableStateOf(false) }
    var loginSuccess by remember { mutableStateOf(true) }
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

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = {
                        Text(
                            "username",
                            fontSize = 15.sp
                        )
                    },

                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                )

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

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = {
                        Text(
                            "password",
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

                Text(
                    if (loginSuccess) "" else "Login failed",
                    color = Color.Red
                )


                Button(
                    onClick = {
                        if (!showGmailField) {
                            db.collection("user").whereEqualTo("username", username)
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
//                            loginToServer(username, password) { success ->
//                                if (success) {
//                                    // 登入成功 切換到下個頁面
//                                    loginSuccess = true
//                                    clickButtonToChat(context, username, password)
//                                } else {
//                                    // 登入失敗 顯示Login failed
//                                    loginSuccess = false
//                                    Log.e("Login", "Login failed")
//                                }
//                            }
//                            clickButtonToChat(context, username, password)
                        } else {
                            db.collection("user").add(User("","01057132@email.ntou.edu.tw",
                                emptyList(),false,"starstar","01057132","zjPjbMzB7uR3AjBcgh2C",
                                "starstar"))
                                .addOnSuccessListener { documentReference ->
                                    Log.d("signup", "success")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("signup", "fail")
                                }
                        }
                    },

//
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
                        onClick = { showGmailField = !showGmailField },
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




fun clickButtonToChat(context: Context, username: String, password: String) {
    val intent = Intent()
    intent.setClassName(context,
        "com.ntou.dokidokichat.FriendsPage")
    intent.putExtra(MainActivity.KEY_USER_NAME, username)
    intent.putExtra(MainActivity.KEY_PASSWORD, password)

    context.startActivity(intent)
}

fun loginToServer(username: String, password: String, onResult: (Boolean) -> Unit) {
    // 登入請求
    val requestBody = FormBody.Builder()
        .add("username", username)
        .add("password", password)
        .build()

    val request = Request.Builder()
        .url(MainActivity.Constants.LoginAPI.URL)
        .post(requestBody)
        .build()

    val client = OkHttpClient()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            // 登入失敗回傳 false
            Log.e("Login", "Failed to login: ${e.message}")
            onResult(false)
        }

        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.peekBody(Long.MAX_VALUE) // Read the entire body into memory as a string
            val responseData = responseBody.string()


            try {
                val jsonResponse = JSONObject(responseData)
                val success = jsonResponse.getBoolean("success")

                // 根据服务器返回的结果调用相应的回调函数
                if (success) {
                    onResult(true) // 登入成功，回傳 true
                } else {
                    onResult(false) // 登入失败，回傳 false
                }
            } catch (e: JSONException) {

                Log.e("Login", "Failed to parse JSON: ${e.message}")
                onResult(false)
            }
        }
    })
}