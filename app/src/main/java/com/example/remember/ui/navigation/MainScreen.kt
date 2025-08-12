package com.example.remember.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.remember.ui.all_tasks.AllTasksScreen
import com.example.remember.ui.settings.SettingsScreen
import com.example.remember.ui.settings.SettingsViewModel
import com.example.remember.ui.today.TodayScreen


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    parentNavController: NavController
) {
    val childNavController = rememberNavController()
    val bottomNavItems = listOf(ChildScreens.Today, ChildScreens.AllTasks, ChildScreens.Settings)
    val context = LocalContext.current

    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val settingsState by settingsViewModel.uiState.collectAsState()
    val settings = settingsState.settings

    val navBackStackEntry by childNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val topBarTitle = when (currentRoute) {
        ChildScreens.Today.route -> "Today's Review"
        ChildScreens.AllTasks.route -> "All Cards"
        ChildScreens.Settings.route -> "Settings"
        else -> "Remember"
    }

    LaunchedEffect(settings) {
        if (settings != null) {
            val hours = settings.notificationTimeMinutes / 60
            val minutes = settings.notificationTimeMinutes % 60
            settingsViewModel.scheduleDailyNotification(context, hours, minutes)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )

        },
        bottomBar = {
            NavigationBar {
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    if (currentDestination != null) {
                        NavigationBarItem(
                            icon = { Icon(screen.icon!!, contentDescription = screen.label) },
                            label = { Text(screen.label!!) },
                            selected = currentDestination.hierarchy.any { it.route == screen.route },
                            onClick = {
                                childNavController.navigate(screen.route) {
                                    popUpTo(childNavController.graph.findStartDestination().id)
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == ChildScreens.AllTasks.route) {
                FloatingActionButton(onClick = { parentNavController.navigate(ParentScreens.AddEditCard.routeToAdd()) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Card")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            childNavController,
            startDestination = ChildScreens.Today.route,
            Modifier.padding(innerPadding)
        ) {
            composable(ChildScreens.Today.route) { TodayScreen(parentNavController = parentNavController) }
            composable(ChildScreens.AllTasks.route) {
                AllTasksScreen(
                    parentNavController = parentNavController,
                    // childNavController = childNavController
                )
            }
            composable(ChildScreens.Settings.route) {
                SettingsScreen(
//                    childNavController = childNavController
                )
            }
        }
    }
}