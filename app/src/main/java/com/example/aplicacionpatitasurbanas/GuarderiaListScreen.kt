package com.example.aplicacionpatitasurbanas

// ▼▼▼ NUEVAS IMPORTACIONES REQUERIDAS ▼▼▼
import android.content.Intent
import android.net.Uri
// ▲▲▲ FIN DE NUEVAS IMPORTACIONES ▲▲▲

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
fun GuarderiaListScreen(
    onRegresar: () -> Unit,
    onVerComentarios: (String) -> Unit
) {
    var guarderias by remember { mutableStateOf<List<GuarderiaConId>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val currentUserId = Firebase.auth.currentUser?.uid
    var isScreenActive by remember { mutableStateOf(true) }

    // Función para manejar los likes/unlikes
    fun toggleLike(guarderiaId: String, likedByList: List<String>) {
        if (currentUserId == null) {
            Toast.makeText(context, "Debes iniciar sesión para dar like", Toast.LENGTH_SHORT).show()
            return
        }
        val db = Firebase.firestore
        val guarderiaRef = db.collection("guarderias").document(guarderiaId)
        val alreadyLiked = likedByList.contains(currentUserId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(guarderiaRef)
            val currentLikesDouble = snapshot.getDouble("likeCount") ?: 0.0
            val currentLikes = currentLikesDouble.toInt()
            val newLikes = if (alreadyLiked) currentLikes - 1 else currentLikes + 1
            val updatedLikedBy = if (alreadyLiked) FieldValue.arrayRemove(currentUserId) else FieldValue.arrayUnion(currentUserId)
            transaction.update(guarderiaRef, "likeCount", newLikes.toDouble())
            transaction.update(guarderiaRef, "likedBy", updatedLikedBy)
            null
        }.addOnFailureListener { e ->
            if (isScreenActive) {
                Toast.makeText(context, "Error al procesar like: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Listener para las guarderías
    DisposableEffect(Unit) {
        isLoading = true
        isScreenActive = true
        val db = Firebase.firestore
        var listenerRegistration: ListenerRegistration? = null

        listenerRegistration = db.collection("guarderias")
            .orderBy("fechaCreacion", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (!isScreenActive) return@addSnapshotListener

                if (error != null) {
                    Toast.makeText(context, "Error al cargar guarderías: ${error.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    guarderias = snapshot.documents.mapNotNull { doc ->
                        val likeCount = doc.getLong("likeCount")?.toInt() ?: 0
                        val commentCount = doc.getLong("commentCount")?.toInt() ?: 0

                        GuarderiaConId(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            ubicacion = doc.getString("ubicacion") ?: "",
                            direccion = doc.getString("direccion") ?: "", // ▼▼▼ LEEMOS EL NUEVO CAMPO ▼▼▼
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
                Text(stringResource(id = R.string.regresar))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Guardería Zone",
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
            } else if (guarderias.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
                    Text("Aún no hay guarderías publicadas")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(guarderias, key = { it.id }) { guarderia ->
                        GuarderiaCard(
                            guarderia = guarderia,
                            currentUserId = currentUserId,
                            onLikeToggle = { toggleLike(guarderia.id, guarderia.likedBy) },
                            onCommentClick = { onVerComentarios(guarderia.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun GuarderiaCard(
    guarderia: GuarderiaConId,
    currentUserId: String?,
    onLikeToggle: () -> Unit,
    onCommentClick: () -> Unit
) {
    val isLiked = currentUserId != null && guarderia.likedBy.contains(currentUserId)

    // ▼▼▼ NECESITAMOS EL CONTEXTO PARA EL INTENT DE MAPS ▼▼▼
    val context = LocalContext.current

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
                text = guarderia.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Ubicación o barrio:", value = guarderia.ubicacion)
            InfoRow(label = "Dirección:", value = guarderia.direccion) // ▼▼▼ MOSTRAMOS LA DIRECCIÓN ▼▼▼
            InfoRow(label = "Tipo de servicios:", value = guarderia.servicio)
            InfoRow(label = "Tipo de calificación:", value = guarderia.calificacion)
            InfoRow(label = "Trato hacia las mascotas:", value = guarderia.tratoMascotas, maxLines = 4)
            InfoRow(label = "Comentarios adicionales:", value = guarderia.comentarios, maxLines = 4)

            Spacer(modifier = Modifier.height(16.dp))

            // --- Botones de Like y Comentario ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween // Para alinear el botón de mapa a la derecha
            ) {
                // --- Grupo de Like y Comentarios (a la izquierda) ---
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLikeToggle) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else Color.Gray
                        )
                    }
                    Text(guarderia.likeCount.toString())

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(onClick = onCommentClick) {
                        Icon(
                            imageVector = Icons.Filled.Comment,
                            contentDescription = stringResource(id = R.string.comentarios),
                            tint = Color.Gray
                        )
                    }
                    Text(guarderia.commentCount.toString())
                }

                // ▼▼▼ NUEVO BOTÓN PARA VER EN MAPA (a la derecha) ▼▼▼
                TextButton(
                    onClick = {
                        // Creamos el Intent para Google Maps
                        val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(guarderia.direccion)}")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                        mapIntent.setPackage("com.google.android.apps.maps") // Fuerza a usar Google Maps

                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            // Por si no tiene Google Maps instalado
                            Toast.makeText(context, "No se pudo abrir Google Maps.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = guarderia.direccion.isNotBlank() // Solo se activa si hay una dirección
                ) {
                    Text("Ver en Mapa")
                }
                // ▲▲▲ FIN DE NUEVO BOTÓN ▲▲▲
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, maxLines: Int = 1) {
    if (value.isNotBlank()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(label, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, maxLines = maxLines)
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}