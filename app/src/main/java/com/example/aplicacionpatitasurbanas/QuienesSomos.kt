package com.example.aplicacionpatitasurbanas

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.sp
import com.example.aplicacionpatitasurbanas.ui.theme.FondoLilac
import androidx.compose.ui.text.TextStyle
import com.example.aplicacionpatitasurbanas.ui.theme.RubikPuddles
import androidx.compose.ui.res.stringResource

@Composable
fun QuienesSomos(
    onIniciarSesion: () -> Unit = {}
) {
    val scroll = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoLilac)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // 👈 centra verticalmente el contenido
        ) {
            // Logo
            Image(
                painter = painterResource(id = R.drawable.ellipse_1),
                contentDescription = stringResource(id = R.string.quienes_somos_logo_desc),
                modifier = Modifier.size(200.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(20.dp))

            // Título
            Text(
                text = stringResource(id = R.string.quienes_somos_titulo),
                style = TextStyle(
                    fontFamily = RubikPuddles,
                    fontSize = 40.sp
                ),
                color = Color(0xFF2E2E2E),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(50.dp))

            // Párrafo con Inter un poco más grande
            Text(
                text = stringResource(id = R.string.quienes_somos_texto),
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp), // 👈 texto más grande
                color = Color(0xFF2E2E2E),
                textAlign = TextAlign.Justify,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(50.dp)) // 👈 sube el botón (menos espacio)

            Button(
                onClick = onIniciarSesion,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF0B4BE),
                    contentColor = Color(0xFF2E2E2E)
                ),
                modifier = Modifier
                    .height(48.dp)
                    .widthIn(min = 180.dp)
            ) {
                Text(stringResource(id = R.string.iniciar_sesion), style = TextStyle(
                    fontSize = 20.sp
                )
                )
            }

            Spacer(Modifier.height(18.dp))
        }
    }
}