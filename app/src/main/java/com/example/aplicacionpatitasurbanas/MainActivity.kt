package com.example.aplicacionpatitasurbanas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.aplicacionpatitasurbanas.ui.theme.PatitasurbanasTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PatitasurbanasTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()
                val startDestination = if (Firebase.auth.currentUser != null) "ingreso_ok" else "pantalla1"

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable(route = "pantalla1") {
                        PantallaPrincipal(navController)
                    }

                    composable("ingreso_ok") {
                        IngresoOkScreen(
                            onSalir = {
                                Firebase.auth.signOut()
                                navController.navigate("pantalla1") {
                                    popUpTo(0) // Limpia todo el backstack
                                }
                            },
                            onConsejosClick = { navController.navigate("nuevo_consejo") },
                            onVerConsejosClick = { navController.navigate("consejos_lista") },
                            onCrearRecetaClick = { navController.navigate("nueva_receta") },
                            onVerRecetasClick = { navController.navigate("recetas_lista") },
                            onEditarPublicacionesClick = { navController.navigate("mis_publicaciones") }
                        )
                    }

                    // --- RUTAS DE CONSEJOS ---
                    composable("consejos_lista") {
                        ConsejosListScreen(
                            onRegresar = { navController.popBackStack() },
                            onVerComentarios = { consejoId ->
                                navController.navigate("comentarios/$consejoId")
                            }
                        )
                    }
                    composable(
                        route = "comentarios/{consejoId}",
                        arguments = listOf(navArgument("consejoId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val consejoId = backStackEntry.arguments?.getString("consejoId") ?: ""
                        ComentariosScreen(
                            consejoId = consejoId,
                            onRegresar = { navController.popBackStack() }
                        )
                    }
                    composable("nuevo_consejo") {
                        NuevoConsejoScreen(
                            onPublicarSuccess = { navController.popBackStack() },
                            onRegresar = { navController.popBackStack() }
                        )
                    }

                    // --- RUTA DE MIS PUBLICACIONES (ACTUALIZADA) ---
                    composable("mis_publicaciones") {
                        MisPublicacionesScreen(
                            onRegresar = { navController.popBackStack() },
                            // Pasa ambas funciones de edición
                            onEditarConsejo = { consejoId ->
                                navController.navigate("editar_consejo/$consejoId")
                            },
                            onEditarReceta = { recetaId ->
                                navController.navigate("editar_receta/$recetaId")
                            }
                        )
                    }

                    // --- RUTA DE EDITAR CONSEJO (Existente) ---
                    composable(
                        route = "editar_consejo/{consejoId}",
                        arguments = listOf(navArgument("consejoId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val consejoId = backStackEntry.arguments?.getString("consejoId") ?: ""
                        EditarConsejoScreen(
                            consejoId = consejoId,
                            onGuardado = { navController.popBackStack() },
                            onRegresar = { navController.popBackStack() }
                        )
                    }


                    // --- RUTAS DE RECETAS ---
                    composable("nueva_receta") {
                        NuevaRecetaScreen(
                            onPublicarSuccess = { navController.popBackStack() },
                            onRegresar = { navController.popBackStack() }
                        )
                    }
                    composable("recetas_lista") {
                        RecetasListScreen(
                            onRegresar = { navController.popBackStack() },
                            onVerComentarios = { recetaId ->
                                navController.navigate("receta_comentarios/$recetaId")
                            }
                        )
                    }
                    composable(
                        route = "receta_comentarios/{recetaId}",
                        arguments = listOf(navArgument("recetaId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val recetaId = backStackEntry.arguments?.getString("recetaId") ?: ""
                        RecetaComentariosScreen(
                            recetaId = recetaId,
                            onRegresar = { navController.popBackStack() }
                        )
                    }

                    // --- ¡NUEVA RUTA DE EDITAR RECETA! ---
                    composable(
                        route = "editar_receta/{recetaId}",
                        arguments = listOf(navArgument("recetaId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val recetaId = backStackEntry.arguments?.getString("recetaId") ?: ""
                        EditarRecetaScreen(
                            recetaId = recetaId,
                            onGuardado = { navController.popBackStack() },
                            onRegresar = { navController.popBackStack() }
                        )
                    }


                    // --- RUTAS DE AUTENTICACIÓN ---
                    composable(route = "pantalla2") {
                        QuienesSomos(
                            onIniciarSesion = { navController.navigate("inicio_sesion") }
                        )
                    }
                    // ... (El resto de tus rutas de inicio_sesion, registro, etc. se quedan igual)
                    composable(route = "inicio_sesion") {
                        InicioSesion(
                            onForgotPasswordClick = {
                                navController.navigate("recuperar_contrasena_1")
                            },
                            onRegisterClick = { navController.navigate("registro") },
                            onLoginSuccess = {
                                navController.navigate("ingreso_ok") {
                                    popUpTo("pantalla1")
                                }
                            }
                        )
                    }
                    composable("registro") {
                        RegistroScreen(
                            onRegisterSuccess = { navController.popBackStack() },
                            onCancelar = { navController.popBackStack() }
                        )
                    }
                    composable("recuperar_contrasena_1") {
                        RecuperarContrasenaPantalla1(
                            onRecuperarClick = { navController.popBackStack() },
                            onCancelarClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}