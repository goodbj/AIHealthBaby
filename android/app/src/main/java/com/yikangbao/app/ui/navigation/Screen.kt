package com.yikangbao.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Chat : Screen("chat")
    data object Records : Screen("records")
    data object Reminder : Screen("reminder")
    data object Profile : Screen("profile")
    data object RecordDetail : Screen("record/{recordId}") {
        fun createRoute(recordId: String) = "record/$recordId"
    }
}
