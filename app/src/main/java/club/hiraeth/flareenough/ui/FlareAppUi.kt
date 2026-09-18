package club.hiraeth.flareenough.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import club.hiraeth.flareenough.R
import club.hiraeth.flareenough.ui.history.HistoryScreen
import club.hiraeth.flareenough.ui.medication.MedicationEditScreen
import club.hiraeth.flareenough.ui.medication.MedicationEditViewModel
import club.hiraeth.flareenough.ui.medication.MedicationListScreen
import club.hiraeth.flareenough.ui.medication.MedicationListViewModel
import club.hiraeth.flareenough.ui.navigation.Routes
import club.hiraeth.flareenough.ui.navigation.TopDestination
import club.hiraeth.flareenough.ui.reminders.ReminderHealthScreen
import club.hiraeth.flareenough.ui.settings.SettingsScreen
import club.hiraeth.flareenough.ui.stillness.StillnessScreen
import club.hiraeth.flareenough.ui.support.rememberAppContainer
import club.hiraeth.flareenough.ui.symptoms.SymptomLogScreen
import club.hiraeth.flareenough.ui.symptoms.SymptomLogViewModel
import club.hiraeth.flareenough.ui.today.TodayScreen
import club.hiraeth.flareenough.ui.today.TodayViewModel

/**
 * Top level navigation. The four tabs and Settings live inside the tabbed shell.
 * Full screen flows like the medication list and editor sit above the shell so
 * they get their own top bar and fill the screen, with no double chrome.
 */
@Composable
fun FlareApp() {
    val rootNav = rememberNavController()

    NavHost(navController = rootNav, startDestination = ROUTE_MAIN) {
        composable(ROUTE_MAIN) {
            MainShell(
                onOpenMedications = { rootNav.navigate(Routes.MEDICATIONS) },
                onOpenReminderHealth = { rootNav.navigate(Routes.REMINDER_HEALTH) },
            )
        }

        composable(Routes.REMINDER_HEALTH) {
            ReminderHealthScreen(onBack = { rootNav.popBackStack() })
        }

        composable(Routes.MEDICATIONS) {
            val container = rememberAppContainer()
            val vm: MedicationListViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { MedicationListViewModel(container.medicationRepository) }
                },
            )
            MedicationListScreen(
                onBack = { rootNav.popBackStack() },
                onAddMedication = { rootNav.navigate(Routes.medicationEdit(0)) },
                onOpenMedication = { id -> rootNav.navigate(Routes.medicationEdit(id)) },
                viewModel = vm,
            )
        }

        composable(
            route = Routes.MEDICATION_EDIT_PATTERN,
            arguments = listOf(
                navArgument(Routes.MEDICATION_EDIT_ARG) { type = NavType.StringType },
            ),
        ) { backStackEntry ->
            val medId = backStackEntry.arguments
                ?.getString(Routes.MEDICATION_EDIT_ARG)
                ?.toLongOrNull() ?: 0L
            val container = rememberAppContainer()
            val vm: MedicationEditViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        MedicationEditViewModel(
                            container.medicationRepository,
                            container.reminderManager,
                            medId,
                        )
                    }
                },
            )
            if (!vm.ready) {
                LoadingScreen()
            } else {
                MedicationEditScreen(
                    onBack = { rootNav.popBackStack() },
                    onSaved = { rootNav.popBackStack() },
                    onDeleted = { rootNav.popBackStack() },
                    viewModel = vm,
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * The tabbed shell: a top bar, four bottom destinations, and Settings reached from
 * the top bar icon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainShell(
    onOpenMedications: () -> Unit,
    onOpenReminderHealth: () -> Unit,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val onSettings = currentRoute == Routes.SETTINGS

    val titleRes = when (currentRoute) {
        Routes.SETTINGS -> R.string.nav_settings
        else -> TopDestination.entries.firstOrNull { it.route == currentRoute }?.labelRes
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
            if (!onSettings) {
                NavigationBar {
                    TopDestination.entries.forEach { destination ->
                        val selected = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == destination.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = null) },
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
            composable(TopDestination.TODAY.route) {
                val container = rememberAppContainer()
                val vm: TodayViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            TodayViewModel(container.medicationRepository, container.doseRepository)
                        }
                    },
                )
                TodayScreen(
                    viewModel = vm,
                    remindersMayBeLate = !container.alarmScheduler.canScheduleExact(),
                    onOpenReminderHealth = onOpenReminderHealth,
                    onQuickSymptom = {
                        navController.navigate(TopDestination.LOG.route) { launchSingleTop = true }
                    },
                    onOpenStillness = {
                        navController.navigate(TopDestination.STILLNESS.route) { launchSingleTop = true }
                    },
                )
            }
            composable(TopDestination.LOG.route) {
                val container = rememberAppContainer()
                val vm: SymptomLogViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer {
                            SymptomLogViewModel(container.symptomRepository, container.dayRepository)
                        }
                    },
                )
                SymptomLogScreen(viewModel = vm)
            }
            composable(TopDestination.HISTORY.route) { HistoryScreen() }
            composable(TopDestination.STILLNESS.route) { StillnessScreen() }
            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onOpenMedications = onOpenMedications,
                    onOpenReminderHealth = onOpenReminderHealth,
                )
            }
        }
    }
}

private const val ROUTE_MAIN = "main"
