package com.gestion.itinerario.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.gestion.itinerario.ui.agenda.AgendaScreen
import com.gestion.itinerario.ui.clients.ClientsScreen
import com.gestion.itinerario.ui.dashboard.DashboardScreen
import com.gestion.itinerario.ui.inventory.InventoryScreen
import com.gestion.itinerario.ui.reminders.RemindersScreen
import com.gestion.itinerario.ui.services.ServicesScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val INVENTORY = "inventory"
    const val CLIENTS = "clients"
    const val SERVICES = "services"
    const val AGENDA = "agenda"
    const val REMINDERS = "reminders"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    onNavigate: (String) -> Unit = {}
) {
    NavHost(navController = navController, startDestination = Routes.DASHBOARD) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                innerPadding = innerPadding,
                onNavigate = onNavigate
            )
        }
        composable(Routes.INVENTORY) { InventoryScreen(innerPadding = innerPadding) }
        composable(Routes.CLIENTS) { ClientsScreen(innerPadding = innerPadding) }
        composable(Routes.SERVICES) { ServicesScreen(innerPadding = innerPadding) }
        composable(Routes.AGENDA) { AgendaScreen(innerPadding = innerPadding) }
        composable(Routes.REMINDERS) { RemindersScreen() }
    }
}
