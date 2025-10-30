package com.example.aplicacionpatitasurbanas

import android.widget.Toast
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionpatitasurbanas.ui.theme.FondoLilac
import com.example.aplicacionpatitasurbanas.ui.theme.RubikPuddles
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarRecetaScreen(
    recetaId: String,
    onGuardado: () -> Unit,
    onRegresar: () -> Unit
) {
    // --- Estados para los campos ---
    var nombre by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var ingredientes by remember { mutableStateOf("") }
    var preparacion by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // --- Listas y estados para Dropdowns ---
    val tiposReceta = listOf(
        "Snacks y premios", "Comidas completas", "Recetas Refrescantes",
        "Especiales", "Masticables", "Funcionales"
    )
    var tipoRecetaSel by remember { mutableStateOf(tiposReceta[0]) }
    var isTipoRecetaExp by remember { mutableStateOf(false) }

    val tiposMascota = listOf("Perros", "Gatos", "Roedores", "Aves", "Otra tipo")
    var tipoMascotaSel by remember { mutableStateOf(tiposMascota[0]) }
    var isTipoMascotaExp by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = Firebase.firestore

    // --- Carga los datos de la receta ---
    LaunchedEffect(Unit) {
        try {
            val doc = db.collection("recetas").document(recetaId).get().await()
            if (doc.exists()) {
                nombre = doc.getString("nombre") ?: ""
                alias = doc.getString("alias") ?: ""
                ingredientes = doc.getString("ingredientes") ?: ""
                preparacion = doc.getString("preparacion") ?: ""
                tipoRecetaSel = doc.getString("tipoReceta") ?: tiposReceta[0]
                tipoMascotaSel = doc.getString("tipoMascota") ?: tiposMascota[0]
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar datos", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    // --- Guarda los cambios ---
    fun handleGuardarCambios() {
        isLoading = true
        val recetaActualizada = mapOf(
            "nombre" to nombre,
            "alias" to alias,
            "ingredientes" to ingredientes,
            "preparacion" to preparacion,
            "tipoReceta" to tipoRecetaSel,
            "tipoMascota" to tipoMascotaSel
        )

        db.collection("recetas").document(recetaId)
            .update(recetaActualizada)
            .addOnSuccessListener {
                isLoading = false
                Toast.makeText(context, context.getString(R.string.cambios_guardados), Toast.LENGTH_SHORT).show()
                onGuardado()
            }
            .addOnFailureListener {
                isLoading = false
                Toast.makeText(context, context.getString(R.string.error_guardar), Toast.LENGTH_SHORT).show()
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoLilac)
            .padding(24.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize() // Ocupa todo el espacio disponible
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally, // Centra los elementos hijos horizontalmente
                verticalArrangement = Arrangement.Center // Centra los elementos hijos verticalmente
            ) {
                // ▼▼▼ CAMBIO 1: Ajusta el Spacer para mover el título más abajo ▼▼▼
                Spacer(Modifier.height(40.dp)) // Incrementado para bajar más el título

                Text(
                    stringResource(id = R.string.editar_receta),
                    style = TextStyle(fontFamily = RubikPuddles, fontSize = 40.sp),
                    color = Color(0xFF2E2E2E)
                )
                Spacer(Modifier.height(32.dp))

                // --- Formulario ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 16.dp), // Añade padding horizontal para que no se pegue a los bordes
                    horizontalAlignment = Alignment.Start // Los campos de texto se alinean a la izquierda dentro de este Column
                ) {

                    Text(stringResource(id = R.string.nombre_receta), fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                    TextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        shape = RoundedCornerShape(12.dp),
                        colors = getTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))

                    Text(stringResource(id = R.string.alias_opcional), fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                    TextField(
                        value = alias,
                        onValueChange = { alias = it },
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
                            modifier = Modifier.menuAnchor().fillMaxWidth()
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
                            modifier = Modifier.menuAnchor().fillMaxWidth()
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
                        shape = RoundedCornerShape(12.dp),
                        colors = getTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                Spacer(Modifier.height(32.dp))

                // --- Botones ---
                Button(
                    onClick = { handleGuardarCambios() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFBDEDBE), // Verde menta claro
                        contentColor = Color.Black
                    )
                ) {
                    Text(stringResource(id = R.string.guardar_cambios))
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onRegresar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0B4BE), // Rosado claro
                        contentColor = Color.Black
                    )
                ) {
                    Text(stringResource(id = R.string.cancelar))
                }
                // ▼▼▼ CAMBIO 2: Añade un Spacer al final para empujar el contenido hacia el centro ▼▼▼
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}