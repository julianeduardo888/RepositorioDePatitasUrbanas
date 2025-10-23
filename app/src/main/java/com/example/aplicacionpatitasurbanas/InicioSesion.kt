package com.example.aplicacionpatitasurbanas

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionpatitasurbanas.ui.theme.FondoLilac
import com.example.aplicacionpatitasurbanas.ui.theme.RubikPuddles
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.res.stringResource

@Composable
fun InicioSesion(
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var errorLogin by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val auth: FirebaseAuth = Firebase.auth

    val canTry = email.isNotBlank() && contrasena.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoLilac)
            .imePadding()
            .navigationBarsPadding()
    ) {
        Image(
            painter = painterResource(id = R.drawable.ellipse_2),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .graphicsLayer(scaleX = 2.8f, scaleY = 2.8f)
                .alpha(0.8f),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .widthIn(max = 500.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.iniciar_sesion),
                style = TextStyle(fontFamily = RubikPuddles, fontSize = 40.sp),
                color = Color(0xFF2E2E2E),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            Text(stringResource(id = R.string.email), style = MaterialTheme.typography.bodyLarge, color = Color(0xFF2E2E2E),
                modifier = Modifier.fillMaxWidth(0.85f))
            Spacer(Modifier.height(6.dp))
            TextField(
                value = email,
                onValueChange = { email = it; errorLogin = null },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(0.85f).height(50.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(stringResource(id = R.string.contrasena), style = MaterialTheme.typography.bodyLarge, color = Color(0xFF2E2E2E),
                modifier = Modifier.fillMaxWidth(0.85f))
            Spacer(Modifier.height(6.dp))
            TextField(
                value = contrasena,
                onValueChange = { contrasena = it; errorLogin = null },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth(0.85f).height(50.dp)
            )

            Spacer(Modifier.height(8.dp))

            if (errorLogin != null) {
                Text(
                    text = errorLogin!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
                Spacer(Modifier.height(8.dp))
            }

            TextButton(
                onClick = onForgotPasswordClick,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4A4A4A)),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(id = R.string.olvidaste_contrasena),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(18.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                auth.signInWithEmailAndPassword(email, contrasena).await()
                                errorLogin = null
                                isLoading = false
                                onLoginSuccess()
                            } catch (e: Exception) {
                                isLoading = false
                                errorLogin = context.getString(R.string.error_login)
                            }
                        }
                    },
                    enabled = canTry,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0B4BE),
                        contentColor = Color(0xFF2E2E2E),
                        disabledContainerColor = Color(0xFFF0B4BE).copy(alpha = 0.5f),
                        disabledContentColor = Color(0xFF2E2E2E).copy(alpha = 0.6f)
                    ),
                    modifier = Modifier.height(48.dp).widthIn(min = 200.dp)
                ) { Text(stringResource(id = R.string.ingresar), style = TextStyle(fontSize = 18.sp)) }
            }


            Spacer(Modifier.height(14.dp))

            Button(
                onClick = onRegisterClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF8B195),
                    contentColor = Color(0xFF2E2E2E)
                ),
                modifier = Modifier.height(48.dp).widthIn(min = 200.dp)
            ) { Text(stringResource(id = R.string.registrarse), style = TextStyle(fontSize = 18.sp)) }
        }
    }
}