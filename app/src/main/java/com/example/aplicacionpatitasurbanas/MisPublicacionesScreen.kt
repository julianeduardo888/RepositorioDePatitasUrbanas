package com.example.aplicacionpatitasurbanas

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
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisPublicacionesScreen(
    onRegresar: () -> Unit,
    onEditar: (String) -> Unit // Recibe el ID del consejo a editar
) {
    // ▼▼▼ CAMBIO 1: Dos listas, una maestra y una filtrada ▼▼▼
    var todosMisConsejos by remember { mutableStateOf<List<ConsejoConId>>(emptyList()) }
    var consejosFiltrados by remember { mutableStateOf<List<ConsejoConId>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // State para el filtro
    val categorias = listOf(stringResource(R.string.cat_todas),
        stringResource(R.string.cat_alimentacion),
        stringResource(R.string.cat_salud),
        stringResource(R.string.cat_comportamiento),
        stringResource(R.string.cat_higiene),
        stringResource(R.string.cat_curiosidades))

    var categoriaSeleccionada by remember { mutableStateOf(categorias[0]) }
    var isExpanded by remember { mutableStateOf(false) }

    // ▼▼▼ CAMBIO 2: Se cargan TODOS los consejos del usuario UNA SOLA VEZ ▼▼▼
    LaunchedEffect(Unit) {
        val db = Firebase.firestore
        val currentUser = Firebase.auth.currentUser

        if (currentUser != null) {
            try {
                val result = db.collection("consejos")
                    .whereEqualTo("autorId", currentUser.uid)
                    .orderBy("fechaCreacion", Query.Direction.DESCENDING)
                    .get().await()

                todosMisConsejos = result.documents.mapNotNull { doc ->
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
                // Al inicio, la lista filtrada es igual a la lista completa
                consejosFiltrados = todosMisConsejos
            } catch (e: Exception) {
                // Manejar error
            } finally {
                isLoading = false
            }
        }
    }

    // ▼▼▼ CAMBIO 3: Un nuevo efecto que se activa CADA VEZ que cambia la categoría seleccionada ▼▼▼
    LaunchedEffect(categoriaSeleccionada) {
        if (categoriaSeleccionada == "Todas") {
            consejosFiltrados = todosMisConsejos
        } else {
            // Filtra la lista maestra localmente
            consejosFiltrados = todosMisConsejos.filter { it.categoria == categoriaSeleccionada }
        }
    }

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

        // --- Filtro Desplegable ---
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it }
        ) {
            TextField(
                value = stringResource(id = R.string.dropdown_categoria, categoriaSeleccionada),
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
                categorias.forEach { categoria ->
                    DropdownMenuItem(
                        text = { Text(categoria) },
                        onClick = {
                            categoriaSeleccionada = categoria
                            isExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (consejosFiltrados.isEmpty()) {
            Text(if (categoriaSeleccionada == stringResource(R.string.cat_todas)) stringResource(R.string.sin_publicaciones_aun) else stringResource(R.string.sin_publicaciones_categoria))
        } else {
            // ▼▼▼ CAMBIO 4: La lista ahora muestra los consejos filtrados ▼▼▼
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(consejosFiltrados) { consejo ->
                    ConsejoEditableCard(consejo = consejo, onEditar = { onEditar(consejo.id) })
                }
            }
        }
    }
}

@Composable
fun ConsejoEditableCard(consejo: ConsejoConId, onEditar: () -> Unit) {
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
                stringResource(id = R.string.card_por, consejo.alias),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(Modifier.height(8.dp))
            Text(
                consejo.descripcion,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onEditar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF0B4BE),
                        contentColor = Color.Black
                    )
                ) {
                    Text(stringResource(id = R.string.editar))
                }
            }
        }
    }
}