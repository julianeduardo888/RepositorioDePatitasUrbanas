package com.example.aplicacionpatitasurbanas.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.aplicacionpatitasurbanas.R

// Fuentes
val RubikPuddles = FontFamily(
    Font(R.font.rubik_puddles_regular, weight = FontWeight.Normal)
)

val InterFamily = FontFamily(
    Font(R.font.inter_regular, weight = FontWeight.Normal)
)

// Mapea estilos que usaremos en las pantallas
val Typography = Typography(
    // Para títulos grandes (como "Quienes somos")
    displayLarge = TextStyle(
        fontFamily = RubikPuddles,
        fontSize = 40.sp,
        lineHeight = 44.sp
    ),
    // Variante de título (por si la necesitas)
    titleLarge = TextStyle(
        fontFamily = RubikPuddles,
        fontSize = 32.sp,
        lineHeight = 36.sp
    ),
    // Texto de párrafo
    bodyLarge = TextStyle(
        fontFamily = InterFamily,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    // Texto de botones
    labelLarge = TextStyle(
        fontFamily = InterFamily,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium
    )
)