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
fun NuevaGuarderiaScreen(
    onPublicarSuccess: () -> Unit,
    onRegresar: () -> Unit
) {
    // --- Estados para los campos ---
    var nombre by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") } // ▼▼▼ NUEVO ESTADO ▼▼▼
    var tratoMascotas by remember { mutableStateOf("") }
    var comentarios by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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

    // ▼▼▼ CAMBIO: Añadimos 'direccion' a la validación ▼▼▼
    val formIsValid = nombre.isNotBlank() && ubicacion.isNotBlank() && tratoMascotas.isNotBlank() && direccion.isNotBlank()

    fun handlePublicar() {
        if (!formIsValid) {
            // ▼▼▼ CAMBIO: Mensaje de validación actualizado ▼▼▼
            Toast.makeText(context, "Nombre, ubicación, dirección y trato son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser

        val nuevaGuarderia = Guarderia(
            nombre = nombre,
            ubicacion = ubicacion,
            direccion = direccion, // ▼▼▼ NUEVO CAMPO A GUARDAR ▼▼▼
            servicio = servicioSel,
            calificacion = calificacionSel,
            tratoMascotas = tratoMascotas,
            comentarios = comentarios,
            autorId = currentUser?.uid
        )

        db.collection("guarderias")
            .add(nuevaGuarderia)
            .addOnSuccessListener {
                isLoading = false
                Toast.makeText(context, "Guardería publicada con éxito", Toast.LENGTH_SHORT).show()
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
                text = "Nueva Guardería",
                style = TextStyle(fontFamily = RubikPuddles, fontSize = 40.sp),
                color = Color(0xFF2E2E2E)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalAlignment = Alignment.Start
            ) {

                Text("Nombre de la guardería:", fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                TextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    placeholder = { Text("Nombre aquí:") },
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                Text("Ubicación o barrio:", fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                TextField(
                    value = ubicacion,
                    onValueChange = { ubicacion = it },
                    placeholder = { Text("Ej: Chapinero, Bogotá") },
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                // ▼▼▼ NUEVO CAMPO DE TEXTO PARA DIRECCIÓN ▼▼▼
                Text("Dirección (para el mapa):", fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                TextField(
                    value = direccion,
                    onValueChange = { direccion = it },
                    placeholder = { Text("Ej: Calle 50 #10-20") },
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                // ▲▲▲ FIN DE NUEVO CAMPO ▲▲▲

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
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
                    placeholder = { Text("Describe el trato...") },
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
                    placeholder = { Text("Escribe tu descripción...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
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