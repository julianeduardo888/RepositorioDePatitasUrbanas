package com.example.aplicacionpatitasurbanas

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionpatitasurbanas.ui.theme.FondoLilac
import com.example.aplicacionpatitasurbanas.ui.theme.RubikPuddles
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaRecetaScreen(
    onPublicarSuccess: () -> Unit,
    onRegresar: () -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var ingredientes by remember { mutableStateOf("") }
    var preparacion by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // --- Listas para Dropdowns (basado en tu imagen image_cb2ab6.png) ---
    val tiposReceta = listOf(
        "Snacks y premios", "Comidas completas", "Recetas Refrescantes",
        "Especiales", "Masticables", "Funcionales"
    )
    var tipoRecetaSel by remember { mutableStateOf(tiposReceta[0]) }
    var isTipoRecetaExp by remember { mutableStateOf(false) }

    val tiposMascota = listOf("Perros", "Gatos", "Roedores", "Aves", "Otra tipo")
    var tipoMascotaSel by remember { mutableStateOf(tiposMascota[0]) } // <-- Variable correcta
    var isTipoMascotaExp by remember { mutableStateOf(false) }

    val context = LocalContext.current
    // Validamos los campos principales
    val formIsValid = nombre.isNotBlank() && ingredientes.isNotBlank() && preparacion.isNotBlank()

    fun handlePublicar() {
        if (!formIsValid) {
            Toast.makeText(context, "Nombre, ingredientes y preparación son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser

        val nuevaReceta = Receta(
            nombre = nombre,
            alias = alias.ifBlank { context.getString(R.string.alias_anonimo) },
            tipoReceta = tipoRecetaSel,
            tipoMascota = tipoMascotaSel,
            ingredientes = ingredientes,
            preparacion = preparacion,
            autorId = currentUser?.uid
        )

        db.collection("recetas") // <-- Guardamos en la nueva colección "recetas"
            .add(nuevaReceta)
            .addOnSuccessListener {
                isLoading = false
                Toast.makeText(context, "Receta publicada con éxito", Toast.LENGTH_SHORT).show()
                onPublicarSuccess()
            }
            .addOnFailureListener { e ->
                isLoading = false
                Toast.makeText(context, "Error al publicar: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoLilac)
            .padding(24.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ellipse_1),
            contentDescription = "Logo",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(80.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(70.dp))

            Text(
                text = stringResource(id = R.string.nueva_receta),
                style = TextStyle(fontFamily = RubikPuddles, fontSize = 40.sp),
                color = Color(0xFF2E2E2E)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.Start
            ) {

                Text(stringResource(id = R.string.nombre_receta), fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                TextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    placeholder = { Text(stringResource(id = R.string.placeholder_nombre_receta)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Text(stringResource(id = R.string.alias_opcional), fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                TextField(
                    value = alias,
                    onValueChange = { alias = it },
                    placeholder = { Text(stringResource(id = R.string.placeholder_alias_receta)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                // --- Dropdown Tipo de Receta ---
                Text(stringResource(id = R.string.tipo_receta), fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                ExposedDropdownMenuBox(
                    expanded = isTipoRecetaExp,
                    onExpandedChange = { isTipoRecetaExp = it }
                ) {
                    TextField(
                        value = tipoRecetaSel,
                        shape = RoundedCornerShape(12.dp),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTipoRecetaExp) },
                        colors = getTextFieldColors(),
                        // ▼▼▼ CORRECCIÓN 1 ▼▼▼
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isTipoRecetaExp,
                        shape = RoundedCornerShape(16.dp),
                        onDismissRequest = { isTipoRecetaExp = false }
                    ) {
                        tiposReceta.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo) },
                                onClick = {
                                    tipoRecetaSel = tipo
                                    isTipoRecetaExp = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // --- Dropdown Tipo de Mascota ---
                Text(stringResource(id = R.string.tipo_mascota), fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                ExposedDropdownMenuBox(
                    expanded = isTipoMascotaExp,
                    onExpandedChange = { isTipoMascotaExp = it }
                ) {
                    TextField(
                        value = tipoMascotaSel,
                        shape = RoundedCornerShape(12.dp),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isTipoMascotaExp) },
                        colors = getTextFieldColors(),
                        // ▼▼▼ CORRECCIÓN 2 ▼▼▼
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isTipoMascotaExp,
                        shape = RoundedCornerShape(16.dp),
                        onDismissRequest = { isTipoMascotaExp = false }
                    ) {
                        tiposMascota.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo) },
                                onClick = {
                                    tipoMascotaSel = tipo
                                    isTipoMascotaExp = false
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // --- Ingredientes ---
                Text(stringResource(id = R.string.ingredientes), fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                TextField(
                    value = ingredientes,
                    onValueChange = { ingredientes = it },
                    placeholder = { Text(stringResource(id = R.string.placeholder_ingredientes)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                )
                Spacer(Modifier.height(16.dp))

                // --- Preparación ---
                Text(stringResource(id = R.string.preparacion), fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                TextField(
                    value = preparacion,
                    onValueChange = { preparacion = it },
                    placeholder = { Text(stringResource(id = R.string.placeholder_preparacion)) },
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(Modifier.height(32.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { handlePublicar() },
                    enabled = formIsValid,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF8B195))
                ) {
                    Text(stringResource(id = R.string.publicar_flecha), fontSize = 18.sp, color = Color(0xFF2E2E2E))
                }
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onRegresar,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0B4BE))
            ) {
                Text(stringResource(id = R.string.regresar), fontSize = 18.sp, color = Color(0xFF2E2E2E))
            }
        }
    }
}