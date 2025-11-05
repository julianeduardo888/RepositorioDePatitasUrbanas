package com.example.aplicacionpatitasurbanas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionpatitasurbanas.ui.theme.FondoLilac
import com.example.aplicacionpatitasurbanas.ui.theme.RubikPuddles
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPublicacionesScreen(
    onRegresar: () -> Unit,
    onEditarConsejo: (String) -> Unit,
    onEditarReceta: (String) -> Unit,
    onEditarGuarderia: (String) -> Unit // ▼▼▼ NUEVO PARÁMETRO ▼▼▼
) {
    // --- Estados para las listas ---
    var todosMisConsejos by remember { mutableStateOf<List<ConsejoConId>>(emptyList()) }
    var todosMisRecetas by remember { mutableStateOf<List<RecetaConId>>(emptyList()) }
    var todosMisGuarderias by remember { mutableStateOf<List<GuarderiaConId>>(emptyList()) } // ▼▼▼ NUEVO ESTADO ▼▼▼
    var isLoading by remember { mutableStateOf(true) }

    // --- Estados para Borrar ---
    var isDeleting by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = Firebase.firestore

    // --- Estados para el Dropdown (ACTUALIZADO) ---
    val tiposPublicacion = listOf(
        stringResource(id = R.string.mis_consejos),
        stringResource(id = R.string.mis_recetas),
        stringResource(id = R.string.mis_guarderias) // ▼▼▼ NUEVA OPCIÓN ▼▼▼
    )
    var tipoSeleccionado by remember { mutableStateOf(tiposPublicacion[0]) }
    var isExpanded by remember { mutableStateOf(false) }

    // --- Carga TODOS los tipos de publicaciones al inicio ---
    LaunchedEffect(Unit) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            isLoading = false
            return@LaunchedEffect
        }
        val uid = currentUser.uid

        // Cargar Consejos
        try {
            val resultConsejos = db.collection("consejos")
                .whereEqualTo("autorId", uid)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get().await()
            todosMisConsejos = resultConsejos.toObjects(ConsejoConId::class.java)
        } catch (e: Exception) { /* ... */ }

        // Cargar Recetas
        try {
            val resultRecetas = db.collection("recetas")
                .whereEqualTo("autorId", uid)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get().await()
            todosMisRecetas = resultRecetas.toObjects(RecetaConId::class.java)
        } catch (e: Exception) { /* ... */ }

        // ▼▼▼ NUEVA CARGA DE GUARDERÍAS ▼▼▼
        try {
            val resultGuarderias = db.collection("guarderias")
                .whereEqualTo("autorId", uid)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get().await()
            todosMisGuarderias = resultGuarderias.toObjects(GuarderiaConId::class.java)
        } catch (e: Exception) { /* ... */ }
        // ▲▲▲ FIN DE NUEVA CARGA ▲▲▲

        isLoading = false
    }

    // --- Función para manejar el BORRADO (ACTUALIZADA) ---
    fun handleDelete(tipo: String, id: String) {
        isDeleting = true
        coroutineScope.launch {
            try {
                // ▼▼▼ Lógica de borrado actualizada ▼▼▼
                val collectionPath = when (tipo) {
                    "consejo" -> "consejos"
                    "receta" -> "recetas"
                    "guarderia" -> "guarderias"
                    else -> throw IllegalArgumentException("Tipo desconocido")
                }

                // (NOTA: Esto aún no borra subcolecciones como 'comentarios')
                db.collection(collectionPath).document(id).delete().await()

                // Actualizar la UI localmente
                when (tipo) {
                    "consejo" -> todosMisConsejos = todosMisConsejos.filterNot { it.id == id }
                    "receta" -> todosMisRecetas = todosMisRecetas.filterNot { it.id == id }
                    "guarderia" -> todosMisGuarderias = todosMisGuarderias.filterNot { it.id == id }
                }
                // ▲▲▲ FIN DE LÓGICA ACTUALIZADA ▲▲▲

                Toast.makeText(context, context.getString(R.string.publicacion_eliminada), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.error_eliminar, e.message), Toast.LENGTH_LONG).show()
            } finally {
                isDeleting = false
                showDeleteDialog = null
            }
        }
    }


    // --- Diálogo de Confirmación (Sin cambios) ---
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = null },
            title = { Text(stringResource(id = R.string.confirmar_eliminacion_titulo)) },
            text = { Text(stringResource(id = R.string.confirmar_eliminacion_texto)) },
            confirmButton = {
                Button(
                    onClick = {
                        val (tipo, id) = showDeleteDialog!!
                        handleDelete(tipo, id)
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f))
                ) {
                    Text(stringResource(id = R.string.borrar))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null },
                    enabled = !isDeleting
                ) {
                    Text(stringResource(id = R.string.cancelar))
                }
            }
        )
    }

    // --- UI Principal ---
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoLilac)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            TextButton(
                onClick = onRegresar,
                modifier = Modifier.align(Alignment.Start),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.textButtonColors(containerColor = Color(0xFFF0B4BE))
            ) { Text(stringResource(id = R.string.regresar), color = Color(0xFF2E2E2E)) }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.mis_publicaciones),
                style = TextStyle(fontFamily = RubikPuddles, fontSize = 32.sp),
                color = Color(0xFF2E2E2E),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- Dropdown de Tipo de Publicación ---
            ExposedDropdownMenuBox(
                expanded = isExpanded,
                onExpandedChange = { isExpanded = it }
            ) {
                TextField(
                    value = stringResource(id = R.string.tipo_publicacion, tipoSeleccionado),
                    onValueChange = {},
                    readOnly = true,
                    shape = RoundedCornerShape(16.dp),
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) },
                    colors = getTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = isExpanded,
                    shape = RoundedCornerShape(16.dp),
                    onDismissRequest = { isExpanded = false }
                ) {
                    tiposPublicacion.forEach { tipo ->
                        DropdownMenuItem(
                            text = { Text(tipo) },
                            onClick = {
                                tipoSeleccionado = tipo
                                isExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Lógica de Vistas (ACTUALIZADA) ---
            if (isLoading) {
                // El indicador de carga se superpone (ver abajo)
            } else if (tipoSeleccionado == stringResource(id = R.string.mis_consejos)) {
                // --- Muestra Consejos ---
                if (todosMisConsejos.isEmpty()) {
                    Text(stringResource(id = R.string.sin_consejos_propios))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(todosMisConsejos, key = { it.id }) { consejo ->
                            ConsejoEditableCard(
                                consejo = consejo,
                                onEditar = { onEditarConsejo(consejo.id) },
                                onBorrar = { showDeleteDialog = "consejo" to consejo.id }
                            )
                        }
                    }
                }
            } else if (tipoSeleccionado == stringResource(id = R.string.mis_recetas)) {
                // --- Muestra Recetas ---
                if (todosMisRecetas.isEmpty()) {
                    Text(stringResource(id = R.string.sin_recetas_propias))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(todosMisRecetas, key = { it.id }) { receta ->
                            RecetaEditableCard(
                                receta = receta,
                                onEditar = { onEditarReceta(receta.id) },
                                onBorrar = { showDeleteDialog = "receta" to receta.id }
                            )
                        }
                    }
                }
            } else if (tipoSeleccionado == stringResource(id = R.string.mis_guarderias)) {
                // ▼▼▼ NUEVO BLOQUE PARA GUARDERÍAS ▼▼▼
                if (todosMisGuarderias.isEmpty()) {
                    Text("Aún no has publicado guarderías.") // Puedes añadir esto a strings.xml
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(todosMisGuarderias, key = { it.id }) { guarderia ->
                            GuarderiaEditableCard(
                                guarderia = guarderia,
                                onEditar = { onEditarGuarderia(guarderia.id) },
                                onBorrar = { showDeleteDialog = "guarderia" to guarderia.id }
                            )
                        }
                    }
                }
                // ▲▲▲ FIN DE NUEVO BLOQUE ▲▲▲
            }
        }

        if (isLoading || isDeleting) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

// --- Card de Consejo (Sin cambios) ---
@Composable
fun ConsejoEditableCard(
    consejo: ConsejoConId,
    onEditar: () -> Unit,
    onBorrar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                consejo.titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(id = R.string.card_por, consejo.alias.ifEmpty { stringResource(id = R.string.alias_anonimo) }),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                consejo.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 4
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onEditar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0B4BE),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(id = R.string.editar))
                }

                Button(
                    onClick = onBorrar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF8B195),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(id = R.string.borrar))
                }
            }
        }
    }
}

// --- Card de Receta (Sin cambios) ---
@Composable
fun RecetaEditableCard(
    receta: RecetaConId,
    onEditar: () -> Unit,
    onBorrar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                receta.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(id = R.string.card_por, receta.alias.ifEmpty { stringResource(id = R.string.alias_anonimo) }),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(id = R.string.ingredientes),
                fontWeight = FontWeight.Bold
            )
            Text(
                receta.ingredientes,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onEditar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0B4BE),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(id = R.string.editar))
                }
                Button(
                    onClick = onBorrar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF8B195),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(id = R.string.borrar))
                }
            }
        }
    }
}

// ▼▼▼ NUEVO CARD PARA GUARDERÍA ▼▼▼
@Composable
fun GuarderiaEditableCard(
    guarderia: GuarderiaConId,
    onEditar: () -> Unit,
    onBorrar: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                guarderia.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))

            Text("Ubicación:", fontWeight = FontWeight.Bold)
            Text(
                guarderia.ubicacion,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))

            Text("Servicio:", fontWeight = FontWeight.Bold)
            Text(
                guarderia.servicio,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )
            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onEditar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0B4BE),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(id = R.string.editar))
                }

                Button(
                    onClick = onBorrar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF8B195),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(id = R.string.borrar))
                }
            }
        }
    }
}