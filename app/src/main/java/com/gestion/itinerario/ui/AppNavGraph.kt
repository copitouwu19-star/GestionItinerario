package com.gestion.itinerario.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gestion.itinerario.ui.agenda.AgendaScreen
import com.gestion.itinerario.ui.auth.ForgotPasswordScreen
import com.gestion.itinerario.ui.auth.LoginScreen
import com.gestion.itinerario.ui.auth.RegisterScreen
import com.gestion.itinerario.ui.clients.ClientsScreen
import com.gestion.itinerario.ui.dashboard.DashboardScreen
import com.gestion.itinerario.ui.profile.ProfileScreen
import com.gestion.itinerario.ui.reminders.RemindersScreen
import com.gestion.itinerario.ui.services.ServicesScreen

object Routes {
    const val LOGIN           = "login"
    const val REGISTER        = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val DASHBOARD       = "dashboard"
    const val INVENTORY       = "inventory"
    const val CLIENTS         = "clients"
    const val SERVICES        = "services"
    const val AGENDA          = "agenda"
    const val REMINDERS       = "reminders"
    const val PROFILE         = "profile"

    val authRoutes = setOf(LOGIN, REGISTER, FORGOT_PASSWORD)
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    isLoggedIn: Boolean,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit = {}
) {
    val startDestination = if (isLoggedIn) Routes.DASHBOARD else Routes.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {

        // ── Auth ──────────────────────────────────────────────────────────────
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToRegister = { navController.navigate(Routes.REGISTER) },
                onGoToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(onBack = { navController.popBackStack() })
        }

        // ── Main ──────────────────────────────────────────────────────────────
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                innerPadding = innerPadding,
                onNavigate = onNavigate,
                onLogout = {
                    onLogout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.CLIENTS)   { ClientsScreen(innerPadding = innerPadding) }
        composable(Routes.SERVICES)  { ServicesScreen(innerPadding = innerPadding) }
        composable(Routes.AGENDA)    { AgendaScreen(innerPadding = innerPadding) }
        composable(Routes.REMINDERS) { RemindersScreen(innerPadding = innerPadding) }
        composable(Routes.PROFILE) {
            ProfileScreen(innerPadding = innerPadding, onBack = { navController.popBackStack() })
        }
    }
}
