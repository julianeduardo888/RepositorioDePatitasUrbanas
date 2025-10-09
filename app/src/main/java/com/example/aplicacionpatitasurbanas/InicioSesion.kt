package com.example.aplicacionpatitasurbanas

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionpatitasurbanas.ui.theme.FondoLilac
import com.example.aplicacionpatitasurbanas.ui.theme.RubikPuddles

@Composable
fun InicioSesion(
    // Parámetro de acción agregado
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var usuario by remember { mutableStateOf(value = "") }
    var contrasena by remember { mutableStateOf(value = "") }

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
                .fillMaxWidth(1f)
                .graphicsLayer(
                    scaleX = 2.8f,
                    scaleY = 2.8f
                )
                .alpha(100f),
            contentScale = ContentScale.Fit
        )


        // Contenido centrado (FORMULARIO)
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                // Limita el ancho en dispositivos grandes (RESPONSIVIDAD)
                .widthIn(max = 500.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título
            Text(
                text = "Iniciar sesión",
                style = TextStyle(
                    fontFamily = RubikPuddles,
                    fontSize = 40.sp
                ),
                color = Color(0xFF2E2E2E),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))

            // Usuario
            Text(
                "Usuario",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF2E2E2E),
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(Modifier.height(6.dp))
            TextField(
                value = usuario,
                onValueChange = { usuario = it },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Contraseña
            Text(
                "Contraseña",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFF2E2E2E),
                modifier = Modifier.fillMaxWidth(0.85f)
            )
            Spacer(Modifier.height(6.dp))
            TextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(50.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Botón "¿Olvidaste la contraseña?"
            TextButton(
                onClick = onForgotPasswordClick, // ¡AQUÍ ESTÁ LA CONEXIÓN!
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF4A4A4A)),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "¿Olvidaste la contraseña?",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(18.dp))

            // Botón Ingresar
            Button(
                onClick = { /* login  */ },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF0B4BE),
                    contentColor = Color(0xFF2E2E2E)
                ),
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = 200.dp)
            ) { Text("Ingresar", style = TextStyle(
                fontSize = 18.sp))}

            Spacer(Modifier.height(14.dp))

            // Botón Registrarse
            Button(
                onClick = onRegisterClick,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF8B195),
                    contentColor = Color(0xFF2E2E2E)
                ),
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = 200.dp)
            ) { Text("Registrarse", style = TextStyle(
                fontSize = 18.sp))}
        }
    }
}