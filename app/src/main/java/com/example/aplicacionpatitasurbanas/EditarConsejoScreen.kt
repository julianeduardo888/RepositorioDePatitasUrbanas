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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionpatitasurbanas.ui.theme.FondoLilac
import com.example.aplicacionpatitasurbanas.ui.theme.RubikPuddles
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarConsejoScreen(
    consejoId: String,
    onGuardado: () -> Unit,
    onRegresar: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var alias by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var tipoMascota by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val db = Firebase.firestore
        try {
            val doc = db.collection("consejos").document(consejoId).get().await()
            if (doc.exists()) {
                titulo = doc.getString("titulo") ?: ""
                alias = doc.getString("alias") ?: ""
                descripcion = doc.getString("descripcion") ?: ""
                tipoMascota = doc.getString("tipoMascota") ?: ""
                categoriaSeleccionada = doc.getString("categoria") ?: ""
            }
        } catch (e: Exception) {
            // Manejar error
        } finally {
            isLoading = false
        }
    }

    fun handleGuardarCambios() {
        isLoading = true
        val db = Firebase.firestore
        val consejoActualizado = mapOf(
            "titulo" to titulo,
            "alias" to alias,
            "descripcion" to descripcion,
            "tipoMascota" to tipoMascota,
            "categoria" to categoriaSeleccionada
        )

        db.collection("consejos").document(consejoId)
            .update(consejoActualizado)
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
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    stringResource(id = R.string.editar_consejo),
                    style = TextStyle(fontFamily = RubikPuddles, fontSize = 40.sp),
                    color = Color(0xFF2E2E2E)
                )
                Spacer(Modifier.height(32.dp))

                TextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text(stringResource(id = R.string.label_titulo)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors()
                )
                Spacer(Modifier.height(16.dp))
                TextField(
                    value = alias,
                    onValueChange = { alias = it },
                    label = { Text(stringResource(id = R.string.label_alias)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors()
                )
                Spacer(Modifier.height(16.dp))
                TextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text(stringResource(id = R.string.label_descripcion)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors()
                )
                Spacer(Modifier.height(16.dp))
                TextField(
                    value = tipoMascota,
                    onValueChange = { tipoMascota = it },
                    label = { Text(stringResource(id = R.string.label_tipo_mascota)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = getTextFieldColors()
                )

                Spacer(Modifier.height(32.dp))

                // ▼▼▼ CAMBIO 3: Botones actualizados con nuevos colores ▼▼▼
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
            }
        }
    }
}