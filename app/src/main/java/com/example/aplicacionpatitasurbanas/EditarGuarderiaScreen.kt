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
fun EditarGuarderiaScreen(
    guarderiaId: String,
    onGuardado: () -> Unit,
    onRegresar: () -> Unit
) {
    // --- Estados para los campos ---
    var nombre by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var tratoMascotas by remember { mutableStateOf("") }
    var comentarios by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // --- Listas y estados para Dropdowns ---
    val servicios = listOf(
        "Guardería de dia", "Hotel / Hospedaje", "Paseos",
        "Peluqueria", "Entrenamiento"
    )
    var servicioSel by remember { mutableStateOf(servicios[0]) }
    var isServicioExp by remember { mutableStateOf(false) }

    val calificaciones = listOf(
        "Super Recomendable", "Recomendable", "Aceptable",
        "Malo", "Muy Malo"
    )
    var calificacionSel by remember { mutableStateOf(calificaciones[0]) }
    var isCalificacionExp by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val db = Firebase.firestore

    // --- Carga los datos de la guardería ---
    LaunchedEffect(Unit) {
        try {
            val doc = db.collection("guarderias").document(guarderiaId).get().await()
            if (doc.exists()) {
                nombre = doc.getString("nombre") ?: ""
                ubicacion = doc.getString("ubicacion") ?: ""
                direccion = doc.getString("direccion") ?: ""
                tratoMascotas = doc.getString("tratoMascotas") ?: ""
                comentarios = doc.getString("comentarios") ?: ""
                servicioSel = doc.getString("servicio") ?: servicios[0]
                calificacionSel = doc.getString("calificacion") ?: calificaciones[0]
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
        val guarderiaActualizada = mapOf(
            "nombre" to nombre,
            "ubicacion" to ubicacion,
            "direccion" to direccion,
            "tratoMascotas" to tratoMascotas,
            "comentarios" to comentarios,
            "servicio" to servicioSel,
            "calificacion" to calificacionSel
        )

        db.collection("guarderias").document(guarderiaId)
            .update(guarderiaActualizada)
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
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(40.dp))

                Text(
                    "Editar Guardería", // Puedes añadir esto a strings.xml si quieres
                    style = TextStyle(fontFamily = RubikPuddles, fontSize = 40.sp),
                    color = Color(0xFF2E2E2E)
                )
                Spacer(Modifier.height(32.dp))

                // --- Formulario ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {

                    Text("Nombre de la guardería:", fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                    TextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        shape = RoundedCornerShape(12.dp),
                        colors = getTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))

                    Text("Ubicación o barrio:", fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                    TextField(
                        value = ubicacion,
                        onValueChange = { ubicacion = it },
                        shape = RoundedCornerShape(12.dp),
                        colors = getTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))

                    Text("Dirección (para el mapa):", fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                    TextField(
                        value = direccion,
                        onValueChange = { direccion = it },
                        shape = RoundedCornerShape(12.dp),
                        colors = getTextFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(16.dp))

                    // --- Dropdown Tipo de Servicios ---
                    Text("Tipo de servicios:", fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                    ExposedDropdownMenuBox(
                        expanded = isServicioExp,
                        onExpandedChange = { isServicioExp = it }
                    ) {
                        TextField(
                            value = servicioSel,
                            shape = RoundedCornerShape(12.dp),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isServicioExp) },
                            colors = getTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isServicioExp,
                            shape = RoundedCornerShape(16.dp),
                            onDismissRequest = { isServicioExp = false }
                        ) {
                            servicios.forEach { tipo ->
                                DropdownMenuItem(
                                    text = { Text(tipo) },
                                    onClick = {
                                        servicioSel = tipo
                                        isServicioExp = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // --- Dropdown Tipo de Calificación ---
                    Text("Tipo de calificación:", fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                    ExposedDropdownMenuBox(
                        expanded = isCalificacionExp,
                        onExpandedChange = { isCalificacionExp = it }
                    ) {
                        TextField(
                            value = calificacionSel,
                            shape = RoundedCornerShape(12.dp),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCalificacionExp) },
                            colors = getTextFieldColors(),
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isCalificacionExp,
                            shape = RoundedCornerShape(16.dp),
                            onDismissRequest = { isCalificacionExp = false }
                        ) {
                            calificaciones.forEach { tipo ->
                                DropdownMenuItem(
                                    text = { Text(tipo) },
                                    onClick = {
                                        calificacionSel = tipo
                                        isCalificacionExp = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    // --- Trato hacia las mascotas ---
                    Text("Trato hacia las mascotas:", fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                    TextField(
                        value = tratoMascotas,
                        onValueChange = { tratoMascotas = it },
                        shape = RoundedCornerShape(12.dp),
                        colors = getTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                    Spacer(Modifier.height(16.dp))

                    // --- Comentarios adicionales ---
                    Text("Comentarios adicionales:", fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                    TextField(
                        value = comentarios,
                        onValueChange = { comentarios = it },
                        shape = RoundedCornerShape(12.dp),
                        colors = getTextFieldColors(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
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
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}