package com.example.londonbuses.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.londonbuses.ui.screens.NearbyStopsScreen
import com.example.londonbuses.ui.screens.RouteSearchScreen
import com.example.londonbuses.ui.screens.SettingsScreen

sealed class Screen(val route: String, val title: String, val icon: @Composable () -> Unit) {
    object RouteSearch : Screen(
        route = "route_search",
        title = "Search",
        icon = { Icon(Icons.Default.DirectionsBus, contentDescription = "Search Route") }
    )
    object NearbyStops : Screen(
        route = "nearby_stops",
        title = "Nearby",
        icon = { Icon(Icons.Default.MyLocation, contentDescription = "Nearby Stops") }
    )
    object Settings : Screen(
        route = "settings",
        title = "Settings",
        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(viewModel: LondonBusesViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("London Buses", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE11B22),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                val items = listOf(Screen.RouteSearch, Screen.NearbyStops, Screen.Settings)
                items.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = screen.icon,
                        label = { Text(screen.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE11B22),
                            selectedTextColor = Color(0xFFE11B22),
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.RouteSearch.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.RouteSearch.route) {
                RouteSearchScreen(viewModel = viewModel)
            }
            composable(Screen.NearbyStops.route) {
                NearbyStopsScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
        }
    }
}
