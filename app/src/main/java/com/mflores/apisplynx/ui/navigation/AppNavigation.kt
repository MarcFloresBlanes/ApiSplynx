package com.mflores.apisplynx.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mflores.apisplynx.data.local.SessionManager
import com.mflores.apisplynx.ui.home.HomeScreen
import com.mflores.apisplynx.ui.login.LoginScreen
import com.mflores.apisplynx.ui.tasks.TasksScreen
import com.mflores.apisplynx.viewmodel.LoginViewModel

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sessionManager = SessionManager(context)
    val token by sessionManager.accessToken.collectAsState(initial = null)

    // Si el token ya existe, vamos directo a Home. 
    // Usamos 'initial = null' para evitar saltos indeseados antes de que DataStore cargue.
    LaunchedEffect(token) {
        if (token != null && navController.currentDestination?.route == Login::class.qualifiedName) {
            navController.navigate(Home) {
                popUpTo(Login) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (token != null) Home else Login
    ) {
        composable<Login> {
            LoginScreen(onLoginSuccess = {
                // Ya no necesitamos navegar aquí, el LaunchedEffect(token) se encargará
                // cuando SessionManager guarde el token.
            })
        }

        composable<Home> {
            HomeScreen(onLogout = {
                scope.launch {
                    sessionManager.clearSession()

                    // Navegamos de vuelta a Login y limpiamos el historial
                    navController.navigate(Login) {
                        popUpTo(Home) { inclusive = true }
                    }
                }
            }, onGoToTasks = {
                navController.navigate(Tasks)
            })
        }

        composable<Tasks> {
            TasksScreen(onLogout = {
                scope.launch {
                    sessionManager.clearSession()
                    navController.navigate(Login) {
                        popUpTo(Tasks) { inclusive = true }
                    }
                }
            })
        }
    }
}
