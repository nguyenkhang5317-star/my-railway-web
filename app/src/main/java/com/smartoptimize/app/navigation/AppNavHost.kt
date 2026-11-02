package com.smartoptimize.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smartoptimize.app.AppContainer
import com.smartoptimize.app.ui.apps.AppDetailScreen
import com.smartoptimize.app.ui.apps.AppDetailViewModel
import com.smartoptimize.app.ui.apps.AppsScreen
import com.smartoptimize.app.ui.apps.AppsViewModel
import com.smartoptimize.app.ui.battery.BatteryScreen
import com.smartoptimize.app.ui.battery.BatteryViewModel
import com.smartoptimize.app.ui.focus.FocusScreen
import com.smartoptimize.app.ui.focus.FocusViewModel
import com.smartoptimize.app.ui.home.HomeScreen
import com.smartoptimize.app.ui.home.HomeViewModel
import com.smartoptimize.app.ui.optimize.OptimizeScreen
import com.smartoptimize.app.ui.optimize.OptimizeViewModel
import com.smartoptimize.app.ui.settings.AboutScreen
import com.smartoptimize.app.ui.settings.PermissionGuideScreen
import com.smartoptimize.app.ui.settings.SettingsScreen
import com.smartoptimize.app.ui.settings.SettingsViewModel
import com.smartoptimize.app.ui.storage.StorageScreen
import com.smartoptimize.app.ui.storage.StorageViewModel
import com.smartoptimize.app.utils.simpleFactory

object Routes {
    const val HOME = "home"
    const val APPS = "apps"
    const val BATTERY = "battery"
    const val SETTINGS = "settings"
    const val OPTIMIZE = "optimize"
    const val FOCUS = "focus"
    const val STORAGE = "storage"
    const val PERMISSION_GUIDE = "permissionGuide"
    const val ABOUT = "about"
    const val APP_DETAIL = "appDetail/{packageName}"

    fun appDetail(packageName: String) = "appDetail/$packageName"

    val topLevel = setOf(HOME, APPS, BATTERY, SETTINGS)
}

private data class TabItem(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    TabItem(Routes.HOME, "Home", Icons.Filled.Home),
    TabItem(Routes.APPS, "Apps", Icons.Filled.Apps),
    TabItem(Routes.BATTERY, "Battery", Icons.Filled.BatteryFull),
    TabItem(Routes.SETTINGS, "Settings", Icons.Filled.Settings)
)

@Composable
fun AppNavHost(container: AppContainer, ownPackageName: String) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in Routes.topLevel

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val currentDestination = navBackStackEntry?.destination
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy
                                ?.any { it.route == tab.route } == true,
                            onClick = {
                                navController.navigate(tab.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                val vm: HomeViewModel = viewModel(
                    factory = simpleFactory { HomeViewModel(container.deviceRepository) }
                )
                HomeScreen(
                    viewModel = vm,
                    onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                    onOpenOptimize = { navController.navigate(Routes.OPTIMIZE) },
                    onOpenStorage = { navController.navigate(Routes.STORAGE) },
                    onOpenFocus = { navController.navigate(Routes.FOCUS) }
                )
            }

            composable(Routes.APPS) {
                val vm: AppsViewModel = viewModel(
                    factory = simpleFactory { AppsViewModel(container.appsRepository) }
                )
                AppsScreen(
                    viewModel = vm,
                    onOpenApp = { pkg -> navController.navigate(Routes.appDetail(pkg)) },
                    onOpenFocus = { navController.navigate(Routes.FOCUS) },
                    onOpenPermissionGuide = { navController.navigate(Routes.PERMISSION_GUIDE) }
                )
            }

            composable(
                route = Routes.APP_DETAIL,
                arguments = listOf(navArgument("packageName") { type = NavType.StringType })
            ) { entry ->
                val packageName = entry.arguments?.getString("packageName").orEmpty()
                val vm: AppDetailViewModel = viewModel(
                    key = "appDetail_$packageName",
                    factory = simpleFactory {
                        AppDetailViewModel(
                            packageName = packageName,
                            appsRepository = container.appsRepository,
                            preferences = container.preferencesRepository
                        )
                    }
                )
                AppDetailScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Routes.BATTERY) {
                val vm: BatteryViewModel = viewModel(
                    factory = simpleFactory {
                        BatteryViewModel(container.deviceRepository, container.appsRepository)
                    }
                )
                BatteryScreen(
                    viewModel = vm,
                    onOpenApp = { pkg -> navController.navigate(Routes.appDetail(pkg)) },
                    onOpenPermissionGuide = { navController.navigate(Routes.PERMISSION_GUIDE) }
                )
            }

            composable(Routes.SETTINGS) {
                val vm: SettingsViewModel = viewModel(
                    factory = simpleFactory {
                        SettingsViewModel(container.appsRepository, container.preferencesRepository)
                    }
                )
                SettingsScreen(
                    viewModel = vm,
                    onOpenPermissionGuide = { navController.navigate(Routes.PERMISSION_GUIDE) },
                    onOpenAbout = { navController.navigate(Routes.ABOUT) }
                )
            }

            composable(Routes.OPTIMIZE) {
                val vm: OptimizeViewModel = viewModel(
                    factory = simpleFactory {
                        OptimizeViewModel(
                            deviceRepository = container.deviceRepository,
                            appsRepository = container.appsRepository,
                            analyzeDevice = container.analyzeDeviceUseCase
                        )
                    }
                )
                OptimizeScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onReviewApps = { navController.navigate(Routes.APPS) },
                    onOpenPermissionGuide = { navController.navigate(Routes.PERMISSION_GUIDE) }
                )
            }

            composable(Routes.FOCUS) {
                val vm: FocusViewModel = viewModel(
                    factory = simpleFactory {
                        FocusViewModel(
                            appsRepository = container.appsRepository,
                            preferences = container.preferencesRepository,
                            ownPackageName = ownPackageName
                        )
                    }
                )
                FocusScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onOpenApp = { pkg -> navController.navigate(Routes.appDetail(pkg)) },
                    onOpenPermissionGuide = { navController.navigate(Routes.PERMISSION_GUIDE) }
                )
            }

            composable(Routes.STORAGE) {
                val vm: StorageViewModel = viewModel(
                    factory = simpleFactory {
                        StorageViewModel(container.deviceRepository, container.appsRepository)
                    }
                )
                StorageScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onOpenApp = { pkg -> navController.navigate(Routes.appDetail(pkg)) },
                    onOpenPermissionGuide = { navController.navigate(Routes.PERMISSION_GUIDE) }
                )
            }

            composable(Routes.PERMISSION_GUIDE) {
                PermissionGuideScreen(onBack = { navController.popBackStack() })
            }

            composable(Routes.ABOUT) {
                AboutScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
