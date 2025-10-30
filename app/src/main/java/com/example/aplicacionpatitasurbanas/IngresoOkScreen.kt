package com.example.aplicacionpatitasurbanas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionpatitasurbanas.ui.theme.RubikPuddles
import androidx.compose.ui.res.stringResource
// ▼▼▼ IMPORTACIONES NUEVAS ▼▼▼
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button

@Composable
fun IngresoOkScreen(
    onSalir: () -> Unit = {},
    onConsejosClick: () -> Unit = {},
    onCrearRecetaClick: () -> Unit = {},
    onGuarderiaClick: () -> Unit = {},
    onVerConsejosClick: () -> Unit = {},
    onVerRecetasClick: () -> Unit = {},
    onEditarPublicacionesClick: () -> Unit = {}
) {
    // Colores
    val fondo = Color(0xFFBC9EC1)
    val salirColor = Color(0xFFE8A7B3)
    val greenMint  = Color(0xFFBDEDBE)
    val pinkSoft   = Color(0xFFF5B2AC)
    val aquaSoft   = Color(0xFFC0F2F5)
    val yellowSoft = Color(0xFFFFE7A8)
    val orangeSoft = Color(0xFFF8C29A)
    val textDark   = Color(0xFF2E2E2E)

    var expanded by remember { mutableStateOf(false) }

    // ▼▼▼ NUEVO ESTADO PARA EL DIÁLOGO ▼▼▼
    var showExitDialog by remember { mutableStateOf(false) }

    // ▼▼▼ NUEVO: DIÁLOGO DE CONFIRMACIÓN DE SALIDA ▼▼▼
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(id = R.string.confirmar_salida_titulo)) },
            text = { Text(stringResource(id = R.string.confirmar_salida_texto)) },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onSalir() // Llama a la función original de salir (que desloguea)
                    },
                    // Usamos el mismo color rojo que el de "Borrar" para consistencia
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                ) {
                    Text(stringResource(id = R.string.salir)) // Reusa el string "Salir"
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExitDialog = false }
                ) {
                    Text(stringResource(id = R.string.cancelar)) // Reusa el string "Cancelar"
                }
            }
        )
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(fondo)
            .padding(horizontal = 24.dp)
    ) {
        // Botón Salir (arriba-izq)
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 12.dp)
        ) {
            TextButton(
                // ▼▼▼ CAMBIO: onSalir ahora muestra el diálogo ▼▼▼
                onClick = { showExitDialog = true },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = salirColor,
                    contentColor = textDark
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) { Text(stringResource(id = R.string.salir)) }
        }

        // Contenido centrado
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(stringResource(id = R.string.pantalla_principal_titulo),
                style = TextStyle(
                    fontFamily = RubikPuddles,
                    fontSize = 40.sp
                ),
                color = textDark,
                textAlign = TextAlign.Center)

            Spacer(Modifier.height(24.dp))

            SectionTitle(stringResource(id = R.string.publica_tus_comentarios), textDark)

            // ——— Lista desplegable ———
            DropRow(
                bg = greenMint,
                emoji = "🐶",
                text = stringResource(id = R.string.realiza_tu_publicacion),
                expanded = expanded,
                onToggle = { expanded = !expanded }
            )

            // Contenido del dropdown
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ActionRow(bg = pinkSoft,   emoji = "🐾", stringResource(id = R.string.consejos_utiles))        { onConsejosClick() }
                    ActionRow(bg = aquaSoft,   emoji = "🦴", text = stringResource(id = R.string.recetas_para_tu_peludo)) { onCrearRecetaClick() }
                    ActionRow(bg = yellowSoft, emoji = "🐕", text = stringResource(id = R.string.guarderia_zone))          { onGuarderiaClick() }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ——— Mira las publicaciones ———
            SectionTitle(stringResource(id = R.string.mira_las_publicaciones), textDark)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionRow(bg = pinkSoft,   emoji = "🐾", text = stringResource(id = R.string.consejos_utiles))        { onVerConsejosClick() }
                ActionRow(bg = aquaSoft,   emoji = "🦴", text = stringResource(id = R.string.recetas_para_tu_peludo)) { onVerRecetasClick() }
                ActionRow(bg = yellowSoft, emoji = "🐕", text = stringResource(id = R.string.guarderia_zone))          { onGuarderiaClick() }
            }

            Spacer(Modifier.height(16.dp))

            SectionTitle(stringResource(id = R.string.personaliza_tus_publicaciones), textDark)
            ActionRow(bg = orangeSoft, emoji = "🎨", text = stringResource(id = R.string.edita_tus_publicaciones)) {
                onEditarPublicacionesClick()
            }
        }
    }
}

// Helpers
@Composable
private fun SectionTitle(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun DropRow(
    bg: Color,
    emoji: String,
    text: String,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(bg, RoundedCornerShape(12.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 22.sp, modifier = Modifier.padding(end = 10.dp))
        Text(text, color = Color(0xFF2E2E2E), modifier = Modifier.weight(1f))
        Icon(
            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = Color(0xFF2E2E2E)
        )
    }
}

@Composable
private fun ActionRow(
    bg: Color,
    emoji: String,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(bg, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 22.sp, modifier = Modifier.padding(end = 10.dp))
        Text(text, color = Color(0xFF2E2E2E))
    }
}