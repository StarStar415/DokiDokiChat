package com.ntou.dokidokichat

import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.graphics.plus
import androidx.core.graphics.times
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.ntou.dokidokichat.ui.theme.DokiDokiChatTheme
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

        setContent {
            LoginScreen()
        }
    }
}

@Composable
fun LoginScreen() {
    var context = LocalContext.current
    Surface(
        color = Color(0xFFFFC1E0),

        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xFFFFD9EC))
                .padding(20.dp),
                // 更改 column 的背景顏色
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
//                    .background(
//                        color = Color.Transparent,
//                        shape = RoundedCornerShape(16.dp))
//                    .border(2.dp, Color.Black),
                contentAlignment = Alignment.Center,
            ){
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .padding(10.dp)
            ){


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
                            val roundedPolygonPath = polygon.toPath().asComposePath()
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



                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "DokiDoki Chat",
                    fontSize = 30.sp,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .paddingFromBaseline(
                            top = 0.dp,
                            bottom = 8.dp
                        ) // 將 Text 上緣對齊到 Column 上緣，下緣間距 8dp
                )


                var username by remember { mutableStateOf("starstar") }
                var password by remember { mutableStateOf("01057132") }
                var gmail by remember { mutableStateOf("01057132@email.ntou.edu.tw") }
                var showGmailField by remember { mutableStateOf(false) }
                var passwordVisibility by remember { mutableStateOf(false) }
//                Text(
//                    if (showGmailField) "Sign up" else "Sign in",
//                    fontSize = 20.sp,
//                    modifier = Modifier
//                        .padding(bottom = 10.dp)
//                        .paddingFromBaseline(
//                            top = 0.dp,
//                            bottom = 8.dp
//                        ) // 將 Text 上緣對齊到 Column 上緣，下緣間距 8dp
//                )

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
                    // TextField 的背景顏色為父元素的背景顏色
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
                        // TextField 的背景顏色為父元素的背景顏色
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




                Button(
                    onClick = {
                        if (!showGmailField) {
                            clickButtonToChat(context,username,password)
                        } else {
//
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