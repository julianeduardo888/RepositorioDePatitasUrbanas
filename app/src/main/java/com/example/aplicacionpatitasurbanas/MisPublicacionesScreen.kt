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
    onEditarReceta: (String) -> Unit
) {
    // --- Estados para las listas ---
    var todosMisConsejos by remember { mutableStateOf<List<ConsejoConId>>(emptyList()) }
    var todosMisRecetas by remember { mutableStateOf<List<RecetaConId>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // ▼▼▼ NUEVOS ESTADOS PARA BORRAR ▼▼▼
    var isDeleting by remember { mutableStateOf(false) }
    // Par de (Tipo: "consejo" o "receta", ID del documento)
    var showDeleteDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = Firebase.firestore

    // --- Estados para el Dropdown ---
    val tiposPublicacion = listOf(
        stringResource(id = R.string.mis_consejos),
        stringResource(id = R.string.mis_recetas)
    )
    var tipoSeleccionado by remember { mutableStateOf(tiposPublicacion[0]) }
    var isExpanded by remember { mutableStateOf(false) }

    // --- Carga AMBOS tipos de publicaciones al inicio ---
    LaunchedEffect(Unit) {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            isLoading = false
            return@LaunchedEffect
        }

        // Cargar Consejos
        try {
            val resultConsejos = db.collection("consejos")
                .whereEqualTo("autorId", currentUser.uid)
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
                )
            }
        } catch (e: Exception) { /* ... */ }

        // Cargar Recetas
        try {
            val resultRecetas = db.collection("recetas")
                .whereEqualTo("autorId", currentUser.uid)
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

        isLoading = false
    }

    // ▼▼▼ NUEVA FUNCIÓN PARA MANEJAR EL BORRADO ▼▼▼
    fun handleDelete(tipo: String, id: String) {
        isDeleting = true
        coroutineScope.launch {
            try {
                // NOTA IMPORTANTE:
                // Esto borra el documento principal (consejo o receta).
                // NO borra las subcolecciones (como 'comentarios').
                // Para eso, se necesitaría una Cloud Function de Firebase.

                val collectionPath = if (tipo == "consejo") "consejos" else "recetas"
                db.collection(collectionPath).document(id).delete().await()

                // Actualizar la UI localmente para que el ítem desaparezca
                if (tipo == "consejo") {
                    todosMisConsejos = todosMisConsejos.filterNot { it.id == id }
                } else {
                    todosMisRecetas = todosMisRecetas.filterNot { it.id == id }
                }
                Toast.makeText(context, context.getString(R.string.publicacion_eliminada), Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, context.getString(R.string.error_eliminar, e.message), Toast.LENGTH_LONG).show()
            } finally {
                isDeleting = false
                showDeleteDialog = null // Cierra el diálogo
            }
        }
    }


    // ▼▼▼ NUEVO: DIÁLOGO DE CONFIRMACIÓN ▼▼▼
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
    Box(modifier = Modifier.fillMaxSize()) { // Box para superponer el indicador de carga
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

            if (isLoading) {
                // No mostramos nada, el indicador de carga se encarga
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
                                // ▼▼▼ Pasamos la acción para mostrar el diálogo ▼▼▼
                                onBorrar = { showDeleteDialog = "consejo" to consejo.id }
                            )
                        }
                    }
                }
            } else {
                // --- Muestra Recetas ---
                if (todosMisRecetas.isEmpty()) {
                    Text(stringResource(id = R.string.sin_recetas_propias))
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(todosMisRecetas, key = { it.id }) { receta ->
                            RecetaEditableCard(
                                receta = receta,
                                onEditar = { onEditarReceta(receta.id) },
                                // ▼▼▼ Pasamos la acción para mostrar el diálogo ▼▼▼
                                onBorrar = { showDeleteDialog = "receta" to receta.id }
                            )
                        }
                    }
                }
            }
        }

        // ▼▼▼ Indicador de carga (para carga inicial Y borrado) ▼▼▼
        if (isLoading || isDeleting) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

// --- Card de Consejo (ACTUALIZADO) ---
@Composable
fun ConsejoEditableCard(
    consejo: ConsejoConId,
    onEditar: () -> Unit,
    onBorrar: () -> Unit // <-- Nuevo parámetro
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

            // ▼▼▼ CAMBIO: Reemplazado Box con Row para dos botones ▼▼▼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Editar
                Button(
                    onClick = onEditar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0B4BE),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f) // Ocupa la mitad del espacio
                ) {
                    Text(stringResource(id = R.string.editar))
                }

                // Botón Borrar
                Button(
                    onClick = onBorrar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF8B195), // Color naranja/rojizo
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.weight(1f) // Ocupa la otra mitad
                ) {
                    Text(stringResource(id = R.string.borrar))
                }
            }
        }
    }
}

// --- Card de Receta (ACTUALIZADO) ---
@Composable
fun RecetaEditableCard(
    receta: RecetaConId,
    onEditar: () -> Unit,
    onBorrar: () -> Unit // <-- Nuevo parámetro
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

            // ▼▼▼ CAMBIO: Reemplazado Box con Row para dos botones ▼▼▼
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón Editar
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

                // Botón Borrar
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