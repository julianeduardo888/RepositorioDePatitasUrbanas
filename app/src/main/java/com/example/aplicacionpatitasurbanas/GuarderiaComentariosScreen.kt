package com.example.aplicacionpatitasurbanas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacionpatitasurbanas.ui.theme.FondoLilac
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
// ▼▼▼ ¡YA NO SE IMPORTAN SimpleDateFormat, Date, NI Locale AQUÍ! ▼▼▼
// (Usaremos la función 'safeFormat' de ComentariosScreen.kt)


// ▼▼▼ CÓDIGO DUPLICADO ELIMINADO (LÍNEAS 37-43) ▼▼▼
// private val dateFormatter = ...
// fun safeFormat(date: Date): String { ... }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuarderiaComentariosScreen(
    guarderiaId: String,
    onRegresar: () -> Unit
) {
    var comentarios by remember { mutableStateOf<List<GuarderiaComentarioConId>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }

    // --- Estados para la nueva reseña ---
    var nuevoTitulo by remember { mutableStateOf("") }
    var nuevoTexto by remember { mutableStateOf("") }
    var nuevaCalificacion by remember { mutableStateOf(0) } // 0 = sin estrellas

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser = Firebase.auth.currentUser
    val anonimoString = stringResource(id = R.string.alias_anonimo)
    val userAlias = currentUser?.email?.split("@")?.get(0) ?: anonimoString

    // Referencias
    val db = Firebase.firestore
    val guarderiaRef = db.collection("guarderias").document(guarderiaId)
    val comentariosRef = guarderiaRef.collection("comentarios")

    // Carga las reseñas
    LaunchedEffect(Unit) {
        isLoading = true
        val listenerRegistration = comentariosRef.orderBy("fechaCreacion", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error al cargar reseñas: ${error.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    comentarios = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(GuarderiaComentarioConId::class.java)?.copy(id = doc.id)
                    }
                }
                isLoading = false
            }
        // onDispose { listenerRegistration.remove() } // Idealmente
    }

    // Función para añadir la reseña
    fun addResenia() {
        if (nuevoTexto.isBlank() || nuevoTitulo.isBlank() || nuevaCalificacion == 0 || currentUser == null) {
            Toast.makeText(context, "Calificación, título y comentario son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        isSending = true
        val comentario = GuarderiaComentario(
            autorId = currentUser.uid,
            autorAlias = userAlias,
            calificacion = nuevaCalificacion,
            titulo = nuevoTitulo,
            texto = nuevoTexto
        )

        coroutineScope.launch {
            try {
                // Usamos un batch para añadir el comentario Y actualizar el contador
                db.runBatch { batch ->
                    // 1. Añade el nuevo comentario
                    batch.set(comentariosRef.document(), comentario)
                    // 2. Incrementa el contador en el documento principal
                    batch.update(guarderiaRef, "commentCount", FieldValue.increment(1))
                }.await()

                // Limpia los campos
                nuevoTitulo = ""
                nuevoTexto = ""
                nuevaCalificacion = 0
            } catch (e: Exception) {
                Toast.makeText(context, "Error al enviar reseña: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isSending = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reseñas de Guardería") },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.regresar))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FondoLilac)
            )
        },
        // ▼▼▼ La barra inferior ahora contiene todo el formulario de entrada ▼▼▼
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    // 1. Selector de Estrellas
                    Text("Tu valoración:", fontWeight = FontWeight.Bold)
                    StarRatingInput(
                        rating = nuevaCalificacion,
                        onRatingChange = { nuevaCalificacion = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Campo de Título
                    TextField(
                        value = nuevoTitulo,
                        onValueChange = { nuevoTitulo = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Título de tu reseña") },
                        shape = RoundedCornerShape(12.dp),
                        colors = getTextFieldColors(),
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Campo de Texto y Botón Enviar
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextField(
                            value = nuevoTexto,
                            onValueChange = { nuevoTexto = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text(stringResource(id = R.string.escribe_comentario)) },
                            colors = getTextFieldColors(),
                            shape = RoundedCornerShape(12.dp),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { addResenia() },
                            enabled = nuevoTexto.isNotBlank() && nuevoTitulo.isNotBlank() && nuevaCalificacion > 0 && !isSending
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Enviar reseña")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoLilac)
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (comentarios.isEmpty()) {
                Text(
                    "Sin reseñas aún. ¡Sé el primero!",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    reverseLayout = false
                ) {
                    items(comentarios, key = { it.id }) { comentario ->
                        ReseniaCard(comentario)
                    }
                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            }
        }
    }
}

// --- Card para mostrar la reseña (basado en image_44f004.png) ---
@Composable
fun ReseniaCard(comentario: GuarderiaComentarioConId) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Fila de Alias y Fecha
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comentario.autorAlias,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    // ▼▼▼ Aquí usamos la función 'safeFormat' original ▼▼▼
                    text = comentario.fechaCreacion?.let { safeFormat(it) } ?: "...",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Valoración (Estrellas)
            Text("Valoración:", fontWeight = FontWeight.Bold)
            StarRatingDisplay(rating = comentario.calificacion)
            Spacer(modifier = Modifier.height(8.dp))

            // Título
            Text("Título: ${comentario.titulo}", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))

            // Comentario
            Text("Comentario: ${comentario.texto}", fontSize = 16.sp)
        }
    }
}


// --- Helper para MOSTRAR estrellas ---
@Composable
fun StarRatingDisplay(
    rating: Int,
    maxRating: Int = 5,
    starColor: Color = Color(0xFFFFC107) // Amarillo/Dorado
) {
    Row {
        for (i in 1..maxRating) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null,
                tint = if (i <= rating) starColor else Color.Gray,
                modifier = Modifier.size(20.dp) // Más pequeñas para display
            )
        }
    }
}

// --- Helper para SELECCIONAR estrellas ---
@Composable
fun StarRatingInput(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    maxRating: Int = 5,
    starColor: Color = Color(0xFFFFC107)
) {
    Row {
        for (i in 1..maxRating) {
            Icon(
                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = "Estrella $i",
                tint = if (i <= rating) starColor else Color.Gray,
                modifier = Modifier
                    .size(32.dp) // Más grandes para input
                    .clickable { onRatingChange(i) }
            )
        }
    }
}