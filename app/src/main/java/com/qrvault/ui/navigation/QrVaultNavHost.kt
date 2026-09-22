package com.qrvault.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.qrvault.ui.LocalAppContainer
import com.qrvault.ui.screens.add.AddQrScreen
import com.qrvault.ui.screens.camera.CameraScreen
import com.qrvault.ui.screens.details.DetailsScreen
import com.qrvault.ui.screens.home.HomeScreen
import com.qrvault.ui.screens.security.PinSetupScreen
import com.qrvault.ui.screens.settings.SettingsScreen
import com.qrvault.ui.screens.viewer.ViewerScreen

@Composable
fun QrVaultNavHost() {
    val container = LocalAppContainer.current
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onAdd = { navController.navigate(Routes.add(0L)) { launchSingleTop = true } },
                onOpen = { navController.navigate(Routes.details(it)) { launchSingleTop = true } },
                onEdit = { navController.navigate(Routes.add(it)) { launchSingleTop = true } },
                onSettings = { navController.navigate(Routes.SETTINGS) { launchSingleTop = true } },
            )
        }
        composable(
            route = Routes.ADD,
            arguments = listOf(
                navArgument(AddArg.ITEM_ID) {
                    type = NavType.LongType
                    defaultValue = 0L
                },
            ),
        ) { entry ->
            val itemId = entry.arguments?.getLong(AddArg.ITEM_ID) ?: 0L
            AddQrScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onTakePhoto = { navController.navigate(Routes.CAMERA) { launchSingleTop = true } },
            )
        }
        composable(
            route = Routes.DETAILS,
            arguments = listOf(navArgument(DetailArg.ITEM_ID) { type = NavType.LongType }),
        ) { entry ->
            val itemId = entry.arguments?.getLong(DetailArg.ITEM_ID) ?: 0L
            DetailsScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
                onOpenFullscreen = { navController.navigate(Routes.viewer(it)) { launchSingleTop = true } },
                onEdit = { navController.navigate(Routes.add(it)) { launchSingleTop = true } },
            )
        }
        composable(
            route = Routes.VIEWER,
            arguments = listOf(navArgument(ViewerArg.ITEM_ID) { type = NavType.LongType }),
        ) { entry ->
            val itemId = entry.arguments?.getLong(ViewerArg.ITEM_ID) ?: 0L
            ViewerScreen(
                itemId = itemId,
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.CAMERA) {
            CameraScreen(
                onBack = { navController.popBackStack() },
                onCaptured = { uri ->
                    container.pendingImage.value = uri
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onPinSetup = { navController.navigate(Routes.PIN_SETUP) { launchSingleTop = true } },
            )
        }
        composable(Routes.PIN_SETUP) {
            PinSetupScreen(onBack = { navController.popBackStack() })
        }
    }
}