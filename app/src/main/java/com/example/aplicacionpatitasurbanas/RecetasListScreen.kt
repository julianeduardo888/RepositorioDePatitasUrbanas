package com.example.aplicacionpatitasurbanas

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun RecetasListScreen(
    onRegresar: () -> Unit,
    onVerComentarios: (String) -> Unit // Para navegar a los comentarios de la receta
) {
    var recetas by remember { mutableStateOf<List<RecetaConId>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val currentUserId = Firebase.auth.currentUser?.uid
    var isScreenActive by remember { mutableStateOf(true) }

    // Función para manejar los likes/unlikes
    fun toggleLike(recetaId: String, likedByList: List<String>) {
        if (currentUserId == null) {
            Toast.makeText(context, "Debes iniciar sesión para dar like", Toast.LENGTH_SHORT).show()
            return
        }
        val db = Firebase.firestore
        val recetaRef = db.collection("recetas").document(recetaId) // <-- Colección "recetas"
        val alreadyLiked = likedByList.contains(currentUserId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(recetaRef)
            val currentLikesDouble = snapshot.getDouble("likeCount") ?: 0.0
            val currentLikes = currentLikesDouble.toInt()
            val newLikes = if (alreadyLiked) currentLikes - 1 else currentLikes + 1
            val updatedLikedBy = if (alreadyLiked) FieldValue.arrayRemove(currentUserId) else FieldValue.arrayUnion(currentUserId)
            transaction.update(recetaRef, "likeCount", newLikes.toDouble())
            transaction.update(recetaRef, "likedBy", updatedLikedBy)
            null
        }.addOnFailureListener { e ->
            if (isScreenActive) {
                Toast.makeText(context, "Error al procesar like: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Listener para las recetas
    DisposableEffect(Unit) {
        isLoading = true
        isScreenActive = true
        val db = Firebase.firestore
        var listenerRegistration: ListenerRegistration? = null

        listenerRegistration = db.collection("recetas") // <-- Colección "recetas"
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (!isScreenActive) return@addSnapshotListener

                if (error != null) {
                    Toast.makeText(context, "Error al cargar recetas: ${error.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    recetas = snapshot.documents.mapNotNull { doc ->
                        val likeCountLong = doc.getLong("likeCount")
                        val likeCountDouble = doc.getDouble("likeCount")
                        val likeCount = likeCountLong?.toInt() ?: likeCountDouble?.toInt() ?: 0

                        RecetaConId( // <-- Mapeamos a RecetaConId
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            alias = doc.getString("alias") ?: "",
                            tipoReceta = doc.getString("tipoReceta") ?: "",
                            tipoMascota = doc.getString("tipoMascota") ?: "",
                            ingredientes = doc.getString("ingredientes") ?: "",
                            preparacion = doc.getString("preparacion") ?: "",
                            autorId = doc.getString("autorId"),
                            likeCount = likeCount,
                            likedBy = doc.get("likedBy") as? List<String> ?: emptyList()
                        )
                    }
                }
                isLoading = false
            }

        onDispose {
            isScreenActive = false
            listenerRegistration?.remove()
        }
    }

    // --- Interfaz de Usuario (UI) ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoLilac)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            TextButton(
                onClick = onRegresar,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = Color(0xFFF0B4BE),
                    contentColor = Color(0xFF2E2E2E)
                ),
                modifier = Modifier.align(Alignment.Start)
            ) {
                Text(stringResource(id = R.string.regresar)) // <-- OK
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(id = R.string.recetas_comunidad), // <-- Error
                style = TextStyle(fontFamily = RubikPuddles, fontSize = 32.sp),
                color = Color(0xFF2E2E2E),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                    CircularProgressIndicator()
                }
            } else if (recetas.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                    Text(stringResource(id = R.string.sin_recetas)) // <-- Error
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(recetas, key = { it.id }) { receta ->
                        RecetaCard( // <-- Usamos el nuevo RecetaCard
                            receta = receta,
                            currentUserId = currentUserId,
                            onLikeToggle = { toggleLike(receta.id, receta.likedBy) },
                            onCommentClick = { onVerComentarios(receta.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// --- Card Específico para Recetas ---
@Composable
fun RecetaCard(
    receta: RecetaConId,
    currentUserId: String?,
    onLikeToggle: () -> Unit,
    onCommentClick: () -> Unit
) {
    val isLiked = currentUserId != null && receta.likedBy.contains(currentUserId)
    val anonimo = stringResource(id = R.string.alias_anonimo) // <-- OK

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
                text = receta.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(id = R.string.card_por, receta.alias.ifEmpty { anonimo }), // <-- OK
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(stringResource(id = R.string.ingredientes), fontWeight = FontWeight.Bold) // <-- Error
            Text(
                text = receta.ingredientes,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 5 // Limitar por si es muy largo
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(stringResource(id = R.string.preparacion), fontWeight = FontWeight.Bold) // <-- Error
            Text(
                text = receta.preparacion,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 8 // Limitar por si es muy largo
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(id = R.string.card_tipo), fontWeight = FontWeight.Bold) // <-- Error
                Spacer(modifier = Modifier.width(4.dp))
                Text(receta.tipoReceta)
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(id = R.string.card_para), fontWeight = FontWeight.Bold) // <-- OK
                Spacer(modifier = Modifier.width(4.dp))
                Text(receta.tipoMascota)
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- Botones de Like y Comentario ---
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
                    Text(receta.likeCount.toString())
                }

                IconButton(onClick = onCommentClick) {
                    Icon(
                        imageVector = Icons.Filled.Comment,
                        contentDescription = stringResource(id = R.string.comentarios), // <-- OK
                        tint = Color.Gray
                    )
                }
            }
        }
    }
}