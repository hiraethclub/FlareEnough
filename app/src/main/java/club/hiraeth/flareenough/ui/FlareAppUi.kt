package club.hiraeth.flareenough.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.ui.history.HistoryScreen
import club.hiraeth.flareenough.ui.log.LogScreen
import club.hiraeth.flareenough.ui.navigation.Routes
import club.hiraeth.flareenough.ui.navigation.TopDestination
import club.hiraeth.flareenough.ui.settings.SettingsScreen
import club.hiraeth.flareenough.ui.stillness.StillnessScreen
import club.hiraeth.flareenough.ui.today.TodayScreen

/**
 * The whole app shell: a top bar, four bottom navigation destinations, and the
 * Settings screen reached from the top bar icon. This is the navigation skeleton
 * for the setup milestone. Real screen content arrives in later milestones.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlareApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val onSettings = currentRoute == Routes.SETTINGS

    val titleRes = when (currentRoute) {
        Routes.SETTINGS -> R.string.nav_settings
        else -> TopDestination.entries
            .firstOrNull { it.route == currentRoute }
            ?.labelRes
            ?: R.string.app_name
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    if (onSettings) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    }
                },
                actions = {
                    if (!onSettings) {
                        IconButton(onClick = {
                            navController.navigate(Routes.SETTINGS) { launchSingleTop = true }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.nav_settings),
                            )
                        }
                    }
                },
            )
        },
        bottomBar = {
            // The bottom bar is hidden on the Settings screen so it feels like a
            // sub screen rather than a fifth tab.
            if (!onSettings) {
                NavigationBar {
                    TopDestination.entries.forEach { destination ->
                        val selected = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    // Standard bottom nav behaviour: keep one copy of
                                    // each destination and restore its state.
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = null,
                                )
                            },
                            label = { Text(stringResource(destination.labelRes)) },
                            alwaysShowLabel = true,
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopDestination.TODAY.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(TopDestination.TODAY.route) { TodayScreen() }
            composable(TopDestination.LOG.route) { LogScreen() }
            composable(TopDestination.HISTORY.route) { HistoryScreen() }
            composable(TopDestination.STILLNESS.route) { StillnessScreen() }
            composable(Routes.SETTINGS) { SettingsScreen() }
        }
    }
}
