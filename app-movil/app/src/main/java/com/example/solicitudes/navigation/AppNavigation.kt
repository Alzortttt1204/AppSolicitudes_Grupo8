package com.example.solicitudes.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.solicitudes.ui.screens.LoginScreen
import com.example.solicitudes.viewmodel.LoginViewModel
import com.example.solicitudes.ui.screens.MenuScreen
import com.example.solicitudes.ui.screens.ListaSolicitudesScreen
import com.example.solicitudes.ui.screens.SolicitudFormScreen
import com.example.solicitudes.viewmodel.SolicitudViewModel
import com.example.solicitudes.viewmodel.TipoCambioViewModel

// Secuencia: Login -> Menú -> ListaSolicitudesScreen -> SolicitudFormScreen
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Un único SolicitudViewModel que comparten el menú, la lista y el formulario
    val solicitudViewModel: SolicitudViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login",
        // Animación al cambiar de pantalla: la nueva entra deslizándose desde la derecha
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut() },
        // Al volver atrás, el movimiento es al revés
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        composable("login") {
            // El LoginViewModel vive solo mientras el login está en la pila:
            // al cerrar sesión se crea uno nuevo y los campos aparecen vacíos
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(navController, loginViewModel)
        }
        composable("menu") {
            // ViewModel de la API externa (tipo de cambio), solo lo usa el menú
            val tipoCambioViewModel: TipoCambioViewModel = viewModel()
            MenuScreen(navController, solicitudViewModel, tipoCambioViewModel)
        }
        composable("lista") {
            ListaSolicitudesScreen(navController, solicitudViewModel)
        }
        composable("formulario") {
            SolicitudFormScreen(navController, solicitudViewModel)
        }
    }
}
