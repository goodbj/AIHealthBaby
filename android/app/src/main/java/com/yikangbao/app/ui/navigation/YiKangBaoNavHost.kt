package com.yikangbao.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.yikangbao.app.ui.screens.chat.ChatScreen
import com.yikangbao.app.ui.screens.home.HomeScreen
import com.yikangbao.app.ui.screens.profile.ProfileScreen
import com.yikangbao.app.ui.screens.records.RecordDetailScreen
import com.yikangbao.app.ui.screens.records.RecordsScreen
import com.yikangbao.app.ui.screens.reminder.ReminderScreen

data class BottomNavItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "首页", Icons.Default.Home),
    BottomNavItem(Screen.Records, "档案", Icons.Outlined.Folder),
    BottomNavItem(Screen.Reminder, "提醒", Icons.Default.Notifications),
    BottomNavItem(Screen.Profile, "我的", Icons.Default.Person)
)

@Composable
fun YiKangBaoNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.screen.route } == true,
                        onClick = {
                            navController.navigate(item.screen.route) {
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
    ) { innerPadding ->
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
}
