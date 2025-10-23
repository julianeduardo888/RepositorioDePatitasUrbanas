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
                // Decide la pantalla inicial basado en si el usuario ya inició sesión
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
                            onEditarPublicacionesClick = { navController.navigate("mis_publicaciones") }
                        )
                    }

                    composable("consejos_lista") {
                        ConsejosListScreen(
                            onRegresar = { navController.popBackStack() },
                            // Pasa la acción para navegar a comentarios
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
                            onPublicarSuccess = {
                                // Regresa a la pantalla principal después de publicar
                                navController.popBackStack()
                            },
                            onRegresar = {
                                navController.popBackStack()
                            }
                        )
                    }

                    // Ruta para ver la lista de "Mis Publicaciones"
                    composable("mis_publicaciones") {
                        MisPublicacionesScreen(
                            onRegresar = { navController.popBackStack() },
                            onEditar = { consejoId ->
                                // Navega a la pantalla de edición pasando el ID
                                navController.navigate("editar_consejo/$consejoId")
                            }
                        )
                    }

                    // Ruta para la pantalla de edición, recibe el ID como argumento
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

                    composable(route = "pantalla2") {
                        QuienesSomos(
                            onIniciarSesion = { navController.navigate("inicio_sesion") }
                        )
                    }

                    composable(route = "inicio_sesion") {
                        InicioSesion(
                            onForgotPasswordClick = {
                                navController.navigate("recuperar_contrasena_1")
                            },
                            onRegisterClick = { navController.navigate("registro") },
                            onLoginSuccess = {
                                navController.navigate("ingreso_ok") {
                                    popUpTo("pantalla1") // Limpia hasta la pantalla principal
                                }
                            }
                        )
                    }

                    composable("registro") {
                        RegistroScreen(
                            onRegisterSuccess = {
                                navController.popBackStack() // vuelve a inicio_sesion
                            },
                            onCancelar = { navController.popBackStack() }
                        )
                    }

                    composable("recuperar_contrasena_1") {
                        // ▼▼▼ ESTA ES LA LÍNEA CORREGIDA ▼▼▼
                        RecuperarContrasenaPantalla1(
                            onRecuperarClick = {
                                // Después de enviar el correo, regresamos al login
                                navController.popBackStack()
                            },
                            onCancelarClick = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}