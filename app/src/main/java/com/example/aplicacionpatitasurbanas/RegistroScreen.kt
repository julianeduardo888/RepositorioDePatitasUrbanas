package com.example.aplicacionpatitasurbanas

import android.widget.Toast
import androidx.compose.foundation.Image
import com.example.aplicacionpatitasurbanas.ui.theme.FondoLilac
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionpatitasurbanas.ui.theme.RubikPuddles
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.res.stringResource

@Composable
fun RegistroScreen(
    onRegisterSuccess: () -> Unit,
    onCancelar: () -> Unit
) {
    var usuario by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val auth: FirebaseAuth = Firebase.auth

    // --- Validaciones ---
    val emailError = email.isNotBlank() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val passError  = pass2.isNotBlank() && pass != pass2
    val passLengthError = pass.isNotBlank() && pass.length < 6
    val formOk = usuario.isNotBlank() && email.isNotBlank() && pass.isNotBlank() &&
            pass2.isNotBlank() && !emailError && !passError && !passLengthError

    val errorColor = Color(0xFFB00020)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoLilac)
            .imePadding()
            .navigationBarsPadding()
    ) {
        // Fondo (centrado y tenue)
        Image(
            painter = painterResource(id = R.drawable.ellipse_2),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .graphicsLayer(scaleX = 2.8f, scaleY = 2.8f),
            contentScale = ContentScale.Fit
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(id = R.string.registrate),
                style = TextStyle(fontFamily = RubikPuddles, fontSize = 40.sp),
                color = Color(0xFF2E2E2E),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(Modifier.height(24.dp))

            // === Usuario (Firebase no lo usa para login, pero puedes guardarlo en otro lado) ===
            Text(stringResource(id = R.string.usuario),
                modifier = Modifier.fillMaxWidth(0.85f))
            Spacer(Modifier.height(6.dp))
            InputBox(
                value = usuario,
                onValueChange = { usuario = it },
                modifier = Modifier.fillMaxWidth(0.85f),
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                placeholder = stringResource(id = R.string.placeholder_usuario)
            )

            Spacer(Modifier.height(12.dp))

            // === Email ===
            Text(stringResource(id = R.string.email), modifier = Modifier.fillMaxWidth(0.85f))
            Spacer(Modifier.height(6.dp))
            InputBox(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(0.85f),
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
                isError = emailError,
                placeholder = stringResource(id = R.string.placeholder_email)
            )
            if (emailError) {
                Text(
                    text = stringResource(id = R.string.error_email_invalido),
                    color = errorColor,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            // === Contraseña ===
            Text(stringResource(id = R.string.contrasena), modifier = Modifier.fillMaxWidth(0.85f))
            Spacer(Modifier.height(6.dp))
            InputBox(
                value = pass,
                onValueChange = { pass = it },
                modifier = Modifier.fillMaxWidth(0.85f),
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
                isPassword = true,
                isError = passLengthError
            )
            if (passLengthError) {
                Text(
                    text = stringResource(id = R.string.error_contrasena_corta),
                    color = errorColor,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(top = 4.dp)
                )
            }


            Spacer(Modifier.height(12.dp))

            // === Confirmar contraseña ===
            Text(stringResource(id = R.string.confirmar_contrasena), modifier = Modifier.fillMaxWidth(0.85f))
            Spacer(Modifier.height(6.dp))
            InputBox(
                value = pass2,
                onValueChange = { pass2 = it },
                modifier = Modifier.fillMaxWidth(0.85f),
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true,
                isError = passError
            )
            if (passError) {
                Text(
                    text = stringResource(id = R.string.error_contrasenas_no_coinciden),
                    color = errorColor,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    auth.createUserWithEmailAndPassword(email, pass).await()
                                    // Registro exitoso
                                    isLoading = false
                                    Toast.makeText(context, context.getString(R.string.registro_exitoso), Toast.LENGTH_SHORT).show()
                                    onRegisterSuccess()
                                } catch (e: Exception) {
                                    // Error en el registro
                                    isLoading = false
                                    Toast.makeText(context, context.getString(R.string.error_registro, e.message), Toast.LENGTH_LONG).show()
                                }
                            }
                        },
                        enabled = formOk,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE8A7B3),
                            contentColor = Color(0xFF2E2E2E),
                            disabledContainerColor = Color(0xFFE8A7B3).copy(alpha = 0.5f),
                            disabledContentColor = Color(0xFF2E2E2E).copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f)
                    ) { Text(stringResource(id = R.string.aceptar)) }

                    Button(
                        onClick = onCancelar,
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF8B195),
                            contentColor = Color(0xFF2E2E2E)
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .weight(1f)
                    ) { Text(stringResource(id = R.string.cancelar)) }
                }
            }
        }
    }
}

/**
 * Campo de entrada con estilo de caja blanca redondeada.
 * Muestra borde rojo y permite placeholder si hay error.
 */
@Composable
private fun InputBox(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isPassword: Boolean = false,
    isError: Boolean = false,
    placeholder: String? = null
) {
    val visual = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .height(50.dp)
            .background(Color.White, shape)
            .then(
                if (isError) Modifier.border(2.dp, Color(0xFFB00020), shape) else Modifier
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = Color(0xFF2E2E2E), fontSize = 16.sp),
            cursorBrush = SolidColor(Color(0xFF2E2E2E)),
            visualTransformation = visual,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty() && !placeholder.isNullOrEmpty()) {
                    Text(
                        text = placeholder,
                        color = Color(0x802E2E2E), // placeholder tenue
                        fontSize = 16.sp
                    )
                }
                inner()
            }
        )
    }
}