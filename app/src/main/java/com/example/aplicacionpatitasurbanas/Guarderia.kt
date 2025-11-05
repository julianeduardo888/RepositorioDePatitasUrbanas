package com.example.aplicacionpatitasurbanas

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Clase para guardar una Guardería nueva en Firestore
data class Guarderia(
    val nombre: String = "",
    val ubicacion: String = "", // Barrio
    val direccion: String = "", // ▼▼▼ NUEVO CAMPO ▼▼▼
    val servicio: String = "",
    val calificacion: String = "", // Calificación del POST (ej: "Super Recomendable")
    val tratoMascotas: String = "",
    val comentarios: String = "", // Comentarios adicionales del POST
    val autorId: String? = null,
    @ServerTimestamp
    val fechaCreacion: Date? = null,
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val commentCount: Int = 0
)

// Clase para leer una Guardería (con su ID) de Firestore
data class GuarderiaConId(
    val id: String = "",
    val nombre: String = "",
    val ubicacion: String = "", // Barrio
    val direccion: String = "", // ▼▼▼ NUEVO CAMPO ▼▼▼
    val servicio: String = "",
    val calificacion: String = "",
    val tratoMascotas: String = "",
    val comentarios: String = "",
    val autorId: String? = null,
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val commentCount: Int = 0
)

// --- Clases de Comentarios (estas se quedan igual) ---

// Clase para guardar un Comentario/Reseña de Guardería
data class GuarderiaComentario(
    val autorId: String? = null,
    val autorAlias: String = "Anónimo",
    val calificacion: Int = 0, // 1-5 estrellas
    val titulo: String = "",
    val texto: String = "",
    @ServerTimestamp
    val fechaCreacion: Date? = null
)

// Clase para leer un Comentario/Reseña de Guardería con su ID
data class GuarderiaComentarioConId(
    val id: String = "",
    val autorId: String? = null,
    val autorAlias: String = "Anónimo",
    val calificacion: Int = 0,
    val titulo: String = "",
    val texto: String = "",
    val fechaCreacion: Date? = null
)