package com.yikangbao.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yikangbao.app.ui.components.AppleBottomNavigation
import com.yikangbao.app.ui.screens.chat.ChatScreen
import com.yikangbao.app.ui.screens.home.HomeScreen
import com.yikangbao.app.ui.screens.profile.ProfileScreen
import com.yikangbao.app.ui.screens.records.RecordDetailScreen
import com.yikangbao.app.ui.screens.records.RecordsScreen
import com.yikangbao.app.ui.screens.reminder.ReminderScreen

@Composable
fun YiKangBaoNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val currentScreen = currentDestination?.route?.let { route ->
        Screen.entries.find { entry ->
            when (entry) {
                is Screen.RecordDetail -> route.startsWith("record/")
                else -> entry.route == route
            }
        }?.route ?: Screen.Home.route
    } ?: Screen.Home.route
    
    val showBottomNav = currentDestination?.route?.let { route ->
        !route.startsWith("record/") && route != Screen.Chat.route
    } ?: true

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                AppleBottomNavigation(
                    navController = navController,
                    currentScreen = currentScreen
                )
            }
        },
        content = { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onNavigateToChat = { navController.navigate(Screen.Chat.route) }
                    )
                }
                composable(Screen.Chat.route) {
                    ChatScreen(
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Records.route) {
                    RecordsScreen(
                        onRecordClick = { recordId ->
                            navController.navigate(Screen.RecordDetail.createRoute(recordId))
                        }
                    )
                }
                composable(
                    route = Screen.RecordDetail.route,
                    arguments = listOf(navArgument("recordId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val recordId = backStackEntry.arguments?.getString("recordId") ?: ""
                    RecordDetailScreen(
                        recordId = recordId,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Reminder.route) {
                    ReminderScreen()
                }
                composable(Screen.Profile.route) {
                    ProfileScreen()
                }
            }
        }
    )
}
