package com.gestion.itinerario.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
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

// Tema azul fijo para las pantallas de autenticación (no cambia con la paleta del usuario)
private val AuthBlueScheme = lightColorScheme(
    primary            = Color(0xFF1565C0),
    onPrimary          = Color.White,
    primaryContainer   = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary          = Color(0xFF0D47A1),
    onSecondary        = Color.White,
    background         = Color(0xFFF5F8FF),
    surface            = Color.White,
    onBackground       = Color(0xFF1A1C1E),
    onSurface          = Color(0xFF1A1C1E),
    onSurfaceVariant   = Color(0xFF44474F),
    outline            = Color(0xFF74777F)
)

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
    onNavigate: (String) -> Unit = {},
    pendingEntityId: String?   = null,
    pendingEntityType: String? = null,
    onNavigationHandled: () -> Unit = {}
) {
    val startDestination = if (isLoggedIn) Routes.DASHBOARD else Routes.LOGIN

    // Cuando llega una notificación, navega al tab correcto
    LaunchedEffect(pendingEntityId, pendingEntityType) {
        if (pendingEntityId != null && isLoggedIn) {
            val target = when (pendingEntityType) {
                "appointment" -> Routes.SERVICES   // abre ServiceDetailScreen
                "reminder"    -> Routes.REMINDERS
                else          -> return@LaunchedEffect
            }
            navController.navigate(target) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState    = true
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // ── Auth (tema azul fijo, independiente de la paleta del usuario) ───────
        composable(Routes.LOGIN) {
            MaterialTheme(colorScheme = AuthBlueScheme) {
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
        }
        composable(Routes.REGISTER) {
            MaterialTheme(colorScheme = AuthBlueScheme) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onGoToLogin = { navController.popBackStack() }
                )
            }
        }
        composable(Routes.FORGOT_PASSWORD) {
            MaterialTheme(colorScheme = AuthBlueScheme) {
                ForgotPasswordScreen(onBack = { navController.popBackStack() })
            }
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
        composable(Routes.CLIENTS)   { ClientsScreen(innerPadding = innerPadding, onNavigateToProfile = { navController.navigate(Routes.PROFILE) }, onLogout = { onLogout(); navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } }) }
        composable(Routes.SERVICES) {
            ServicesScreen(
                innerPadding        = innerPadding,
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onLogout            = { onLogout(); navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } },
                openDetailAppointmentId = if (pendingEntityType == "appointment") pendingEntityId else null,
                onDetailHandled     = onNavigationHandled
            )
        }
        composable(Routes.AGENDA) {
            AgendaScreen(
                innerPadding        = innerPadding,
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onLogout            = { onLogout(); navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } },
                openAppointmentId   = if (pendingEntityType == "appointment") pendingEntityId else null,
                onOpenHandled       = onNavigationHandled
            )
        }
        composable(Routes.REMINDERS) {
            RemindersScreen(
                innerPadding      = innerPadding,
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onLogout          = { onLogout(); navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } },
                openReminderId    = if (pendingEntityType == "reminder") pendingEntityId else null,
                onOpenHandled     = onNavigationHandled
            )
        }
        composable(Routes.PROFILE) {
            ProfileScreen(innerPadding = innerPadding, onBack = { navController.popBackStack() })
        }
    }
}
