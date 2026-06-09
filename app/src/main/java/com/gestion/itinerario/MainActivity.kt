package com.gestion.itinerario

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.gestion.itinerario.ui.AppNavGraph
import com.gestion.itinerario.ui.Routes
import com.gestion.itinerario.ui.theme.GestionItinerarioTheme
import com.gestion.itinerario.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

data class NavItem(val route: String, val label: String, val icon: ImageVector)

val bottomNavItems = listOf(
    NavItem(Routes.DASHBOARD,  "Inicio",    Icons.Default.Dashboard),
    NavItem(Routes.CLIENTS,    "Clientes",  Icons.Default.People),
    NavItem(Routes.SERVICES,   "Servicios", Icons.Default.Build),
    NavItem(Routes.AGENDA,     "Agenda",    Icons.Default.CalendarMonth),
    NavItem(Routes.REMINDERS,  "Recordar",  Icons.Default.NotificationsActive),
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        enableEdgeToEdge()
        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val paletteId by themeViewModel.paletteId.collectAsStateWithLifecycle()
            GestionItinerarioTheme(paletteId = paletteId) {
                var isLoggedIn by remember {
                    mutableStateOf(FirebaseAuth.getInstance().currentUser != null)
                }
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = currentRoute !in Routes.authRoutes

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (showBottomBar) {
                            GradientBottomNavBar(
                                items = bottomNavItems,
                                isSelected = { item ->
                                    navBackStackEntry?.destination?.hierarchy
                                        ?.any { it.route == item.route } == true
                                },
                                onItemClick = { item ->
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    AppNavGraph(
                        navController  = navController,
                        innerPadding   = innerPadding,
                        isLoggedIn     = isLoggedIn,
                        onLogout       = { isLoggedIn = false },
                        onNavigate     = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Barra de navegación inferior tipo "píldora" con un círculo flotante de gradiente
 * que resalta la pestaña activa (inspirada en el diseño de referencia).
 */
@Composable
private fun GradientBottomNavBar(
    items: List<NavItem>,
    isSelected: (NavItem) -> Boolean,
    onItemClick: (NavItem) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 20.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val gradient = androidx.compose.ui.graphics.Brush.linearGradient(
                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
            )
            items.forEach { item ->
                val selected = isSelected(item)
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onItemClick(item) }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(gradient, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = item.label, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                    } else {
                        Icon(item.icon, contentDescription = item.label,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                        Text(item.label, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
