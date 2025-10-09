package com.example.aplicacionpatitasurbanas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.aplicacionpatitasurbanas.ui.theme.PatitasurbanasTheme

// --------------------------------------------------------

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            PatitasurbanasTheme(darkTheme = false, dynamicColor = false) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = "pantalla1") {

                    composable(route = "pantalla1") { PantallaPrincipal(navController) }

                    composable(route = "pantalla2") {
                        // Debes asegurarte que QuienesSomos acepte este parámetro
                        QuienesSomos(onIniciarSesion = {
                            navController.navigate("inicio_sesion")
                        })
                    }

                    // 1. RUTA INICIO DE SESIÓN: Corregida para recibir la función de navegación
                    composable(route = "inicio_sesion") {
                        InicioSesion(
                            // Pasa la acción para navegar a la primera pantalla de recuperación
                            onForgotPasswordClick = { navController.navigate("recuperar_contrasena_1") },
                            onRegisterClick = {navController.navigate("registro") }
                        )
                    }

                    composable("registro") {
                        RegistroScreen(
                            onAceptar = { _, _, _ -> navController.popBackStack() },
                            onCancelar = { navController.popBackStack() }
                        )
                    }


                    // 2. RUTA RECUPERAR CONTRASEÑA PASO 1 (Pedir Email)
                    composable("recuperar_contrasena_1") {
                        RecuperarContrasenaPantalla1(
                            onRecuperarClick = { email ->
                                // Navega a la Pantalla 2, pasando el email como argumento
                                navController.navigate("recuperar_contrasena_2/$email")
                            },
                            onCancelarClick = {
                                navController.popBackStack() // Vuelve a InicioSesion
                            }
                        )
                    }

                    // 3. RUTA RECUPERAR CONTRASEÑA PASO 2 (Confirmación Email)
                    composable(
                        route = "recuperar_contrasena_2/{email}",
                        arguments = listOf(navArgument("email") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val email = backStackEntry.arguments?.getString("email") ?: ""
                        RecuperarContrasenaPantalla2(
                            email = email,
                            onConfirmarClick = {
                                // Navega a la Pantalla 3 para cambiar la contraseña
                                navController.navigate("recuperar_contrasena_3")
                            },
                            onCancelarClick = {
                                navController.popBackStack() // Vuelve a Pantalla 1 de recuperación
                            }
                        )
                    }

                    // 4. RUTA RECUPERAR CONTRASEÑA PASO 3 (Cambiar Contraseña)
                    composable("recuperar_contrasena_3") {
                        RecuperarContrasenaPantalla3(
                            onAceptarClick = { nuevaContrasena ->
                                // Lógica de cambio de contraseña. Luego, regresa a inicio_sesion.
                                // Esto limpia las pantallas de recuperación del historial.
                                navController.popBackStack(route = "inicio_sesion", inclusive = true)
                                // Si quieres ir a menú principal, usa: navController.navigate("menu_principal")
                            },
                            onCancelarClick = {
                                navController.popBackStack() // Vuelve a Pantalla 2 de recuperación
                            }
                        )
                    }
                }
            }
        }
    }
}