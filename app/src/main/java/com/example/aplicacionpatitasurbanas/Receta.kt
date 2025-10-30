package com.example.aplicacionpatitasurbanas

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

// Clase para guardar una Receta nueva en Firestore
data class Receta(
    val nombre: String = "",
    val alias: String = "",
    val tipoReceta: String = "",
    val tipoMascota: String = "",
    val ingredientes: String = "",
    val preparacion: String = "",
    val autorId: String? = null,
    @ServerTimestamp
    val fechaCreacion: Date? = null,
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList()
)

// Clase para leer una Receta (con su ID) de Firestore
data class RecetaConId(
    val id: String = "",
    val nombre: String = "",
    val alias: String = "",
    val tipoReceta: String = "",
    val tipoMascota: String = "",
    val ingredientes: String = "",
    val preparacion: String = "",
    val autorId: String? = null,
    val likeCount: Int = 0,
    val likedBy: List<String> = emptyList()
)