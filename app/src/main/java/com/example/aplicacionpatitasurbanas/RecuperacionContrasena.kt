package com.example.aplicacionpatitasurbanas // Puedes mantenerlo en el paquete principal si quieres, pero este es más limpio

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.foundation.border
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.aplicacionpatitasurbanas.ui.theme.FondoLilac
import com.example.aplicacionpatitasurbanas.ui.theme.RubikPuddles

// Asegúrate de que FondoLilac esté definido/importado.

// Función auxiliar para el color de los campos de texto
@Composable
fun getTextFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White
)

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
                .graphicsLayer(scaleX = 2.8f, scaleY = 2.8f)
                .alpha(100f),        // ✅ alpha válido (0..1)
            contentScale = ContentScale.Fit
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .widthIn(max = 500.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            content = content
        )
    }
}



// --- PANTALLA 1: Pedir Email ---
@Composable
fun RecuperarContrasenaPantalla1(
    onRecuperarClick: (String) -> Unit,
    onCancelarClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }
    val validateEmail: () -> Boolean = {
        // Validación: el email no debe estar vacío Y debe contener el símbolo '@'
        val isValid = email.isNotBlank() && email.contains("@")
        isEmailValid = isValid // Actualiza el estado de error
        isValid
    }

    RecuperacionContenedor {
        Text(
            text = "Recordar contraseña",
            style = TextStyle(
                fontFamily = RubikPuddles,
                fontSize = 40.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Campo de Email
        Text("Email", modifier = Modifier.fillMaxWidth(0.85f))
        Spacer(Modifier.height(6.dp))
        TextField(
            value = email,
            onValueChange = { email = it
                isEmailValid = true},
            placeholder = { Text("email@ejemplo.com") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(16.dp),
            colors = getTextFieldColors(),
            modifier = Modifier.fillMaxWidth(0.85f).height(50.dp),
            isError = !isEmailValid
        )
        if (!isEmailValid) {
            Text(
                text = "Formato de correo inválido (debe contener @)",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth(0.85f).padding(top = 4.dp)
            )
        }
        Spacer(Modifier.height(30.dp))

        // Botones (manteniendo el ancho del 85% del contenedor)
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { if (validateEmail()) {
                    onRecuperarClick(email) // Si es válido, navega
                } },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(130.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF0B4BE),
                    contentColor = Color(0xFF2E2E2E)
                )
            ) { Text("Recuperar") }

            OutlinedButton(
                onClick = onCancelarClick,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(130.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF8B195),
                    contentColor = Color(0xFF2E2E2E)
                ),
                border = null
            ) { Text("Cancelar") }
        }
    }
}

// --- PANTALLA 2: Confirmación de Email ---
@Composable
fun RecuperarContrasenaPantalla2(
    email: String,
    onConfirmarClick: () -> Unit,
    onCancelarClick: () -> Unit
) {
    RecuperacionContenedor {
        Text(
            text = "Recordar contraseña (verificacion)",
            style = TextStyle(
                fontFamily = RubikPuddles,
                fontSize = 40.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Muestra el Email (como un campo de texto no editable)
        Text("Email", modifier = Modifier.fillMaxWidth(0.85f))
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(50.dp)
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(text = email)
        }

        Spacer(Modifier.height(30.dp))

        // Botones
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onConfirmarClick,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(130.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF0B4BE),
                    contentColor = Color(0xFF2E2E2E)
                )
            ) { Text("Aceptar") }

            OutlinedButton(
                onClick = onCancelarClick,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(130.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF8B195),
                    contentColor = Color(0xFF2E2E2E)
                ),
                border = null
            ) { Text("Cancelar") }
        }
    }
}

// --- PANTALLA 3: Establecer Nueva Contraseña ---
@Composable
fun RecuperarContrasenaPantalla3(
    onAceptarClick: (nuevaContrasena: String) -> Unit,
    onCancelarClick: () -> Unit
) {
    var contrasenaNueva by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }

    // Validaciones
    val errorColor = MaterialTheme.colorScheme.error
    val passError = confirmarContrasena.isNotBlank() && contrasenaNueva != confirmarContrasena
    val formOk = contrasenaNueva.isNotBlank() && confirmarContrasena.isNotBlank() && !passError

    RecuperacionContenedor {
        Text(
            text = "Recordar contraseña",
            style = TextStyle(fontFamily = RubikPuddles, fontSize = 40.sp),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Contraseña
        Text("Contraseña", modifier = Modifier.fillMaxWidth(0.85f))
        Spacer(Modifier.height(6.dp))
        TextField(
            value = contrasenaNueva,
            onValueChange = { contrasenaNueva = it },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            colors = getTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(50.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Confirmar contraseña
        Text("Confirmar contraseña", modifier = Modifier.fillMaxWidth(0.85f))
        Spacer(Modifier.height(6.dp))
        TextField(
            value = confirmarContrasena,
            onValueChange = { confirmarContrasena = it },
            singleLine = true,
            isError = passError,
            visualTransformation = PasswordVisualTransformation(),
            shape = RoundedCornerShape(16.dp),
            colors = getTextFieldColors(),
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(50.dp)
                .then(
                    if (passError)
                        Modifier.border(2.dp, errorColor, RoundedCornerShape(16.dp))
                    else Modifier
                )
        )
        // Mensaje de error
        if (passError) {
            Text(
                text = "Las contraseñas no coinciden",
                color = errorColor,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .padding(top = 4.dp)
            )
        }

        Spacer(Modifier.height(30.dp))

        // Botones
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { onAceptarClick(contrasenaNueva) },
                enabled = formOk, // ✅ solo habilitado cuando todo OK
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF0B4BE),
                    contentColor = Color(0xFF2E2E2E),
                    disabledContainerColor = Color(0xFFF0B4BE).copy(alpha = 0.5f),
                    disabledContentColor = Color(0xFF2E2E2E).copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(130.dp)
            ) { Text("Aceptar") }

            OutlinedButton(
                onClick = onCancelarClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF8B195),
                    contentColor = Color(0xFF2E2E2E)
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.width(130.dp),
                border = null
            ) { Text("Cancelar") }
        }
    }
}