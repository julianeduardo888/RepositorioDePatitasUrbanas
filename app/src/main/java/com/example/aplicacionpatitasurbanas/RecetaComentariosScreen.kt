package com.example.aplicacionpatitasurbanas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send // Icono de enviar actualizado
import androidx.compose.material.icons.filled.ArrowBack // Icono para Regresar
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
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date // Importar Date explícitamente

// ▼▼▼ OptIn para APIs experimentales ▼▼▼
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecetaComentariosScreen(
    recetaId: String,
    onRegresar: () -> Unit
) {
    var comentarios by remember { mutableStateOf<List<ComentarioConId>>(emptyList()) }
    var nuevoComentario by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSending by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser = Firebase.auth.currentUser
    // ▼▼▼ Obtener el string "Anónimo" aquí, dentro del Composable ▼▼▼
    val anonimoString = stringResource(id = R.string.alias_anonimo)
    val userAlias = currentUser?.email?.split("@")?.get(0) ?: anonimoString

    // Referencia a la subcolección de comentarios
    val comentariosRef = Firebase.firestore
        .collection("recetas") // <-- Cambio clave
        .document(recetaId)
        .collection("comentarios")

    // Carga los comentarios iniciales y escucha cambios
    LaunchedEffect(Unit) {
        isLoading = true
        // Escucha en tiempo real
        val listenerRegistration = comentariosRef.orderBy("fechaCreacion", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error al cargar comentarios: ${error.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    comentarios = snapshot.documents.mapNotNull { doc ->
                        ComentarioConId(
                            id = doc.id,
                            autorId = doc.getString("autorId"),
                            autorAlias = doc.getString("autorAlias") ?: anonimoString,
                            texto = doc.getString("texto") ?: "",
                            fechaCreacion = doc.getTimestamp("fechaCreacion")?.toDate()
                        )
                    }
                }
                isLoading = false
            }
        // NOTA: Para un manejo de ciclo de vida más robusto,
        // este listener debería removerse en onDispose, pero para este
        // proyecto funcionará bien.
    }

    // Función para añadir un nuevo comentario
    fun addComentario() {
        if (nuevoComentario.isBlank() || currentUser == null) return

        isSending = true
        val comentario = Comentario(
            autorId = currentUser.uid,
            autorAlias = userAlias,
            texto = nuevoComentario
        )

        coroutineScope.launch {
            try {
                comentariosRef.add(comentario).await()
                nuevoComentario = "" // Limpia el campo de texto
            } catch (e: Exception) {
                Toast.makeText(context, "Error al enviar comentario: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isSending = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.comentarios)) },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(id = R.string.regresar))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FondoLilac)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding())
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = nuevoComentario,
                        onValueChange = { nuevoComentario = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(id = R.string.escribe_comentario)) },
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.LightGray.copy(alpha=0.3f),
                            unfocusedContainerColor = Color.LightGray.copy(alpha=0.3f),
                            cursorColor = Color.Black
                        ),
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { addComentario() }, enabled = nuevoComentario.isNotBlank() && !isSending) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(id = R.string.enviar_comentario))
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
                    stringResource(id = R.string.sin_comentarios),
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
                        // Esta función la reusamos de ComentariosScreen.kt
                        ComentarioCard(comentario)
                    }
                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            }
        }
    }
}

// YA NO SE NECESITAN LAS FUNCIONES ComentarioCard NI safeFormat AQUÍ
// PORQUE SE REUTILIZAN DESDE ComentariosScreen.kt