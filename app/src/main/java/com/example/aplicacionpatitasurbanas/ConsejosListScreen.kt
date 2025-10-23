package com.example.aplicacionpatitasurbanas

import android.util.Log // Importar Log para depuración
import android.widget.Toast
import androidx.compose.foundation.background
// import androidx.compose.foundation.clickable // No se usa directamente aquí
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment // Icono para comentarios
import androidx.compose.material.icons.filled.Favorite // Icono de corazón lleno
import androidx.compose.material.icons.filled.FavoriteBorder // Icono de corazón vacío
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner // Importar LifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle // Importar Lifecycle
import androidx.lifecycle.LifecycleEventObserver // Importar LifecycleEventObserver
import com.example.aplicacionpatitasurbanas.ui.theme.FondoLilac
import com.example.aplicacionpatitasurbanas.ui.theme.RubikPuddles
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration // Importar ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
// import kotlinx.coroutines.launch // No se necesita para toggleLike si usamos SnapshotListener
import kotlinx.coroutines.tasks.await

@Composable
fun ConsejosListScreen(
    onRegresar: () -> Unit,
    onVerComentarios: (String) -> Unit
) {
    var consejos by remember { mutableStateOf<List<ConsejoConId>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val currentUserId = Firebase.auth.currentUser?.uid
    // ▼▼▼ Variable para controlar si el listener debe estar activo ▼▼▼
    var isScreenActive by remember { mutableStateOf(true) }

    // Función para manejar los likes/unlikes (sin cambios)
    fun toggleLike(consejoId: String, likedByList: List<String>) {
        if (currentUserId == null) {
            Toast.makeText(context, "Debes iniciar sesión para dar like", Toast.LENGTH_SHORT).show()
            return
        }
        val db = Firebase.firestore
        val consejoRef = db.collection("consejos").document(consejoId)
        val alreadyLiked = likedByList.contains(currentUserId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(consejoRef)
            val currentLikesDouble = snapshot.getDouble("likeCount") ?: 0.0
            val currentLikes = currentLikesDouble.toInt()
            val newLikes = if (alreadyLiked) currentLikes - 1 else currentLikes + 1
            val updatedLikedBy = if (alreadyLiked) FieldValue.arrayRemove(currentUserId) else FieldValue.arrayUnion(currentUserId)
            transaction.update(consejoRef, "likeCount", newLikes.toDouble())
            transaction.update(consejoRef, "likedBy", updatedLikedBy)
            null
        }.addOnSuccessListener {
            Log.d("FirestoreLike", "Like actualizado con éxito para $consejoId")
        }.addOnFailureListener { e ->
            // Solo muestra el Toast si la pantalla sigue activa
            if (isScreenActive) {
                Toast.makeText(context, "Error al procesar like: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            Log.e("FirestoreLike", "Error al actualizar like para $consejoId", e)
        }
    }

    // Usar DisposableEffect para manejar el listener y su ciclo de vida
    DisposableEffect(Unit) {
        isLoading = true
        isScreenActive = true // Marcar como activo al entrar
        val db = Firebase.firestore
        var listenerRegistration: ListenerRegistration? = null

        listenerRegistration = db.collection("consejos")
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                // ▼▼▼ Comprobación extra: No procesar si la pantalla ya no está activa ▼▼▼
                if (!isScreenActive) {
                    Log.d("FirestoreLoad", "Listener notificado pero pantalla inactiva. Ignorando.")
                    return@addSnapshotListener
                }

                if (error != null) {
                    // Mostrar error solo si ocurre después de la carga inicial Y la pantalla está activa
                    if (!isLoading && isScreenActive) {
                        Log.e("FirestoreLoad", "Error escuchando consejos mientras la pantalla está activa", error)
                        // Considera un Snackbar si necesitas que el mensaje sea más visible
                        Toast.makeText(context, "Error al actualizar consejos: ${error.message}", Toast.LENGTH_SHORT).show()
                    } else if (isLoading) { // Error durante la carga inicial
                        Toast.makeText(context, "Error inicial al cargar consejos: ${error.message}", Toast.LENGTH_LONG).show()
                        Log.e("FirestoreLoad", "Error inicial escuchando consejos", error)
                    }
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    consejos = snapshot.documents.mapNotNull { doc ->
                        val likeCountLong = doc.getLong("likeCount")
                        val likeCountDouble = doc.getDouble("likeCount")
                        val likeCount = likeCountLong?.toInt() ?: likeCountDouble?.toInt() ?: 0

                        ConsejoConId(
                            id = doc.id,
                            titulo = doc.getString("titulo") ?: "",
                            alias = doc.getString("alias") ?: "",
                            categoria = doc.getString("categoria") ?: "",
                            descripcion = doc.getString("descripcion") ?: "",
                            tipoMascota = doc.getString("tipoMascota") ?: "",
                            autorId = doc.getString("autorId"),
                            likeCount = likeCount,
                            likedBy = doc.get("likedBy") as? List<String> ?: emptyList()
                        )
                    }
                    Log.d("FirestoreLoad", "Consejos cargados/actualizados: ${consejos.size}")
                }
                isLoading = false
            }

        // onDispose se ejecuta automáticamente cuando sales de la pantalla
        onDispose {
            Log.d("FirestoreLoad", "Deteniendo listener de consejos (onDispose).")
            isScreenActive = false // Marcar como inactivo
            listenerRegistration?.remove() // Cancela la escucha activa
        }
    }

    // --- Interfaz de Usuario (UI) ---
    // (El código del Box, Column, TextButton, Text, LazyColumn, etc., se mantiene igual que en la versión anterior)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoLilac)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(), // La columna ocupa todo
            horizontalAlignment = Alignment.CenterHorizontally // Centra horizontalmente por defecto
        ) {

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = onRegresar,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xFFF0B4BE),
                    contentColor = Color(0xFF2E2E2E)
                ),
                modifier = Modifier.align(Alignment.Start) // Botón a la izquierda
            ) {
                Text(stringResource(id = R.string.regresar))
            }

            Spacer(modifier = Modifier.height(8.dp)) // Espacio entre botón y título

            Text(
                text = stringResource(id = R.string.consejos_comunidad),
                style = TextStyle(fontFamily = RubikPuddles, fontSize = 32.sp),
                color = Color(0xFF2E2E2E),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth() // Título centrado
            )

            Spacer(modifier = Modifier.height(24.dp)) // Espacio antes de la lista

            if (isLoading) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                    CircularProgressIndicator()
                }
            } else if (consejos.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                    Text(stringResource(id = R.string.sin_consejos))
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f) // Ocupa el espacio restante
                ) {
                    items(consejos, key = { it.id }) { consejo ->
                        ConsejoCard(
                            consejo = consejo,
                            currentUserId = currentUserId,
                            onLikeToggle = { toggleLike(consejo.id, consejo.likedBy) },
                            onCommentClick = { onVerComentarios(consejo.id) }
                        )
                    }
                    // Espacio al final para que no quede pegado al borde inferior
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// La función ConsejoCard no necesita más cambios
@Composable
fun ConsejoCard(
    consejo: ConsejoConId,
    currentUserId: String?,
    onLikeToggle: () -> Unit,
    onCommentClick: () -> Unit
) {
    val isLiked = currentUserId != null && consejo.likedBy.contains(currentUserId)
    val context = LocalContext.current // Necesario para stringResource con parámetros

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = consejo.titulo,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.card_por, consejo.alias ?: stringResource(id = R.string.alias_anonimo)),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = consejo.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(id = R.string.card_categoria), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Text(consejo.categoria)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(id = R.string.card_para), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Text(consejo.tipoMascota)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLikeToggle) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else Color.Gray
                        )
                    }
                    Text(consejo.likeCount.toString())
                }

                IconButton(onClick = onCommentClick) {
                    Icon(
                        imageVector = Icons.Filled.Comment,
                        contentDescription = stringResource(id = R.string.comentarios),
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}