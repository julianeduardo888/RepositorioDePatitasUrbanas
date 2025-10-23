package com.example.aplicacionpatitasurbanas

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Clase original para guardar en Firestore
data class Consejo(
    val titulo: String = "",
    val alias: String = "",
    val categoria: String = "",
    val descripcion: String = "",
    val tipoMascota: String = "",
    val autorId: String? = null,
    @ServerTimestamp
    val fechaCreacion: Date? = null,
    // ▼▼▼ NUEVOS CAMPOS ▼▼▼
    val likeCount: Int = 0, // Contador de likes
    val likedBy: List<String> = emptyList() // Lista de IDs de usuarios que dieron like
)

// Clase para manejar consejos con su ID (usada en las listas)
data class ConsejoConId(
    val id: String = "",
    val titulo: String = "",
    val alias: String = "",
    val categoria: String = "",
    val descripcion: String = "",
    val tipoMascota: String = "",
    val autorId: String? = null,
    // ▼▼▼ NUEVOS CAMPOS ▼▼▼
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList()
)

// ▼▼▼ NUEVA CLASE PARA COMENTARIOS ▼▼▼
data class Comentario(
    val autorId: String? = null,
    val autorAlias: String = "Anónimo", // Guardaremos el alias para mostrarlo fácil
    val texto: String = "",
    @ServerTimestamp
    val fechaCreacion: Date? = null
)

// ▼▼▼ NUEVA CLASE PARA COMENTARIOS CON ID ▼▼▼
data class ComentarioConId(
    val id: String = "",
    val autorId: String? = null,
    val autorAlias: String = "Anónimo",
    val texto: String = "",
    val fechaCreacion: Date? = null
)