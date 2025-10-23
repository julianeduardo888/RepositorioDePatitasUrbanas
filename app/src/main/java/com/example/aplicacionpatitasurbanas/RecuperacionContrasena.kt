package com.example.aplicacionpatitasurbanas

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.aplicacionpatitasurbanas.ui.theme.FondoLilac
import com.example.aplicacionpatitasurbanas.ui.theme.RubikPuddles
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.res.stringResource

// ----------------------------------------------------------------------------------
// PANTALLA 1: Pedir Email (VALIDACIÓN ACTUALIZADA)
// ----------------------------------------------------------------------------------

@Composable
fun getTextFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White
)

// Componente de TextField personalizado para todas las pantallas
@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
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
                if (isError) Modifier.border(2.dp, MaterialTheme.colorScheme.error, shape) else Modifier
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


// Función auxiliar para el estilo del contenedor principal
@Composable
fun RecuperacionContenedor(content: @Composable ColumnScope.() -> Unit) {
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
                .fillMaxWidth(1f)
                .graphicsLayer(scaleX = 2.8f, scaleY = 2.8f),
            contentScale = ContentScale.Fit
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .widthIn(max = 500.dp)
                .verticalScroll(rememberScrollState()), // Permite scroll en pantallas pequeñas
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content
        )
    }
}
@Composable
fun RecuperarContrasenaPantalla1(
    onRecuperarClick: (String) -> Unit,
    onCancelarClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val auth: FirebaseAuth = Firebase.auth
    val context = LocalContext.current

    val validateAndAttemptRecovery: () -> Unit = {
        emailError = null
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = context.getString(R.string.error_email_valido)
        } else {
            isLoading = true
            coroutineScope.launch {
                try {
                    auth.sendPasswordResetEmail(email).await()
                    isLoading = false
                    Toast.makeText(context, context.getString(R.string.correo_recuperacion_enviado), Toast.LENGTH_SHORT).show()
                    onRecuperarClick(email) // O puedes navegar directo al login
                } catch (e: Exception) {
                    isLoading = false
                    emailError = context.getString(R.string.error_recuperacion, e.localizedMessage ?: "Error desconocido")
                }
            }
        }
    }

    RecuperacionContenedor {
        Text(
            text = stringResource(id = R.string.recordar_contrasena),
            style = TextStyle(
                fontFamily = RubikPuddles,
                fontSize = 40.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        Text(stringResource(id = R.string.email), modifier = Modifier.fillMaxWidth(0.85f))
        Spacer(Modifier.height(6.dp))
        CustomTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = null
            },
            placeholder = stringResource(id = R.string.placeholder_email),
            keyboardType = KeyboardType.Email,
            isError = emailError != null,
            modifier = Modifier.fillMaxWidth(0.85f)
        )
        if (emailError != null) {
            Text(
                text = emailError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(top = 4.dp)
            )
        }
        Spacer(Modifier.height(30.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = validateAndAttemptRecovery,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.width(130.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0B4BE),
                        contentColor = Color(0xFF2E2E2E)
                    )
                ) { Text(stringResource(id = R.string.recuperar)) }

                OutlinedButton(
                    onClick = onCancelarClick,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.width(130.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF8B195),
                        contentColor = Color(0xFF2E2E2E)
                    ),
                    border = null
                ) {
                    Text(stringResource(id = R.string.cancelar))
                }
            }
        }
    }
}
