package com.example.aplicacionpatitasurbanas

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
// ▼▼▼ ICONO ACTUALIZADO ▼▼▼
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
import java.text.SimpleDateFormat
import java.util.Date // Importar Date explícitamente
import java.util.Locale

// ▼▼▼ Mover SimpleDateFormat fuera del Composable ▼▼▼
private val dateFormatter = SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault())

// ▼▼▼ OptIn para APIs experimentales ▼▼▼
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComentariosScreen(
    consejoId: String,
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
        .collection("consejos")
        .document(consejoId)
        .collection("comentarios")

    // Carga los comentarios iniciales y escucha cambios
    LaunchedEffect(Unit) {
        isLoading = true
        // Escucha en tiempo real
        val listenerRegistration = comentariosRef.orderBy("fechaCreacion", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Usar el contexto dentro del listener es seguro
                    Toast.makeText(context, "Error al cargar comentarios: ${error.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    comentarios = snapshot.documents.mapNotNull { doc ->
                        ComentarioConId(
                            id = doc.id,
                            autorId = doc.getString("autorId"),
                            // ▼▼▼ Usar la variable anonimoString obtenida antes ▼▼▼
                            autorAlias = doc.getString("autorAlias") ?: anonimoString,
                            texto = doc.getString("texto") ?: "",
                            fechaCreacion = doc.getTimestamp("fechaCreacion")?.toDate()
                        )
                    }
                }
                isLoading = false
            }
        // Asegúrate de remover el listener cuando el Composable se va
        // onDispose { listenerRegistration.remove() } // Esto no funciona bien en LaunchedEffect, mejor usar produceState o ViewModel para manejo complejo de listeners
    }

    // Función para añadir un nuevo comentario
    fun addComentario() {
        if (nuevoComentario.isBlank() || currentUser == null) return

        isSending = true
        val comentario = Comentario(
            autorId = currentUser.uid,
            autorAlias = userAlias,
            texto = nuevoComentario
            // fechaCreacion se añade automáticamente por Firestore (@ServerTimestamp)
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
                title = { Text(stringResource(id = R.string.comentarios)) }, // Usar stringResource
                navigationIcon = {
                    // Usar IconButton para el icono de regresar
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
                        // Añadir padding para evitar que el teclado cubra el campo
                        .padding(bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding())
                        .padding(horizontal = 8.dp, vertical = 8.dp), // Aumentar padding vertical
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = nuevoComentario,
                        onValueChange = { nuevoComentario = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(id = R.string.escribe_comentario)) }, // Usar stringResource
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedContainerColor = Color.LightGray.copy(alpha=0.3f),
                            unfocusedContainerColor = Color.LightGray.copy(alpha=0.3f),
                            cursorColor = Color.Black // Hacer cursor visible
                        ),
                        shape = RoundedCornerShape(24.dp),
                        // Limitar líneas por si acaso
                        maxLines = 5
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = { addComentario() }, enabled = nuevoComentario.isNotBlank() && !isSending) {
                        // ▼▼▼ Usar el icono actualizado ▼▼▼
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = stringResource(id = R.string.enviar_comentario)) // Usar stringResource
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
                    stringResource(id = R.string.sin_comentarios), // Usar stringResource
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    // Para que al aparecer el teclado, la lista haga scroll
                    reverseLayout = false // Asegúrate que no esté invertido
                ) {
                    items(comentarios, key = { it.id }) { comentario ->
                        ComentarioCard(comentario)
                    }
                    // Añadir espacio al final para que el último comentario no quede pegado al input
                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            }
        }
    }
}


@Composable
fun ComentarioCard(comentario: ComentarioConId) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comentario.autorAlias,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    // ▼▼▼ Manejar posible fecha nula de forma segura ▼▼▼
                    text = comentario.fechaCreacion?.let { safeFormat(it) } ?: "...",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = comentario.texto, fontSize = 16.sp)
        }
    }
}

// Función auxiliar segura para formatear fecha, por si acaso
fun safeFormat(date: Date): String {
    return try {
        dateFormatter.format(date)
    } catch (e: Exception) {
        "Fecha inválida"
    }
}

// ▼▼▼ Añadir nuevos strings a strings.xml ▼▼▼
/*
<resources>
    ...
    <string name="comentarios">Comentarios</string>
    <string name="escribe_comentario">Escribe un comentario...</string>
    <string name="enviar_comentario">Enviar comentario</string>
    <string name="sin_comentarios">No hay comentarios aún. ¡Sé el primero!</string>
    <string name="error_cargar_comentarios">Error al cargar comentarios: %1$s</string>
    <string name="error_enviar_comentario">Error al enviar comentario: %1$s</string>
    ...
</resources>
*/