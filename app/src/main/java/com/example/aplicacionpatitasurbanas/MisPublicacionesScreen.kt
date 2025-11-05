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
    onEditarGuarderia: (String) -> Unit
) {
    // --- Estados para las listas ---
    var todosMisConsejos by remember { mutableStateOf<List<ConsejoConId>>(emptyList()) }
    var todosMisRecetas by remember { mutableStateOf<List<RecetaConId>>(emptyList()) }
    var todosMisGuarderias by remember { mutableStateOf<List<GuarderiaConId>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    var isDeleting by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = Firebase.firestore

    val tiposPublicacion = listOf(
        stringResource(id = R.string.mis_consejos),
        stringResource(id = R.string.mis_recetas),
        stringResource(id = R.string.mis_guarderias)
    )
    var tipoSeleccionado by remember { mutableStateOf(tiposPublicacion[0]) }
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            isLoading = false
            return@LaunchedEffect
        }
        val uid = currentUser.uid

        // Cargar Consejos (Tu código original - seguro)
        try {
            val resultConsejos = db.collection("consejos")
                .whereEqualTo("autorId", uid)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get().await()
            todosMisConsejos = resultConsejos.documents.mapNotNull { doc ->
                ConsejoConId(
                    id = doc.id,
                    titulo = doc.getString("titulo") ?: "",
                    alias = doc.getString("alias") ?: "",
                    categoria = doc.getString("categoria") ?: "",
                    descripcion = doc.getString("descripcion") ?: "",
                    tipoMascota = doc.getString("tipoMascota") ?: "",
                    autorId = doc.getString("autorId")
                    // (Los campos de like/comment no se muestran aquí, así que está bien)
                )
            }
        } catch (e: Exception) { /* ... */ }

        // Cargar Recetas (Tu código original - seguro)
        try {
            val resultRecetas = db.collection("recetas")
                .whereEqualTo("autorId", uid)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get().await()
            todosMisRecetas = resultRecetas.documents.mapNotNull { doc ->
                RecetaConId(
                    id = doc.id,
                    nombre = doc.getString("nombre") ?: "",
                    alias = doc.getString("alias") ?: "",
                    tipoReceta = doc.getString("tipoReceta") ?: "",
                    tipoMascota = doc.getString("tipoMascota") ?: "",
                    ingredientes = doc.getString("ingredientes") ?: "",
                    preparacion = doc.getString("preparacion") ?: "",
                    autorId = doc.getString("autorId")
                )
            }
        } catch (e: Exception) { /* ... */ }

        // ▼▼▼ CAMBIO IMPORTANTE AQUÍ ▼▼▼
        // Reemplazamos el toObjects() por un mapeo manual seguro
        try {
            val resultGuarderias = db.collection("guarderias")
                .whereEqualTo("autorId", uid)
                .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                .get().await()

            todosMisGuarderias = resultGuarderias.documents.mapNotNull { doc ->
                // Mapeo manual seguro, igual que en GuarderiaListScreen
                val likeCount = doc.getLong("likeCount")?.toInt() ?: 0
                val commentCount = doc.getLong("commentCount")?.toInt() ?: 0

                GuarderiaConId(
                    id = doc.id, // <-- Obtenemos el ID real
                    nombre = doc.getString("nombre") ?: "",
                    ubicacion = doc.getString("ubicacion") ?: "",
                    direccion = doc.getString("direccion") ?: "",
                    servicio = doc.getString("servicio") ?: "",
                    calificacion = doc.getString("calificacion") ?: "",
                    tratoMascotas = doc.getString("tratoMascotas") ?: "",
                    comentarios = doc.getString("comentarios") ?: "",
                    autorId = doc.getString("autorId"),
                    likeCount = likeCount,
                    commentCount = commentCount,
                    likedBy = doc.get("likedBy") as? List<String> ?: emptyList()
                )
            }
        } catch (e: Exception) {
            // Informar si el índice sigue faltando
            Toast.makeText(context, "Error al cargar guarderías: ${e.message}", Toast.LENGTH_LONG).show()
        }
        // ▲▲▲ FIN DEL CAMBIO ▲▲▲

        isLoading = false
    }

    // --- Función para manejar el BORRADO (Sin cambios) ---
    fun handleDelete(tipo: String, id: String) {
        isDeleting = true
        coroutineScope.launch {
            try {
                val collectionPath = when (tipo) {
                    "consejo" -> "consejos"
                    "receta" -> "recetas"
                    "guarderia" -> "guarderias"
                    else -> throw IllegalArgumentException("Tipo desconocido")
                }

                db.collection(collectionPath).document(id).delete().await()

                when (tipo) {
                    "consejo" -> todosMisConsejos = todosMisConsejos.filterNot { it.id == id }
                    "receta" -> todosMisRecetas = todosMisRecetas.filterNot { it.id == id }
                    "guarderia" -> todosMisGuarderias = todosMisGuarderias.filterNot { it.id == id }
                }

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

    // --- UI Principal (Sin cambios) ---
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

            if (isLoading) {
                // El indicador de carga se superpone
            } else if (tipoSeleccionado == stringResource(id = R.string.mis_consejos)) {
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
                if (todosMisGuarderias.isEmpty()) {
                    Text("Aún no has publicado guarderías.")
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

// --- Card de Guardería (Sin cambios) ---
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