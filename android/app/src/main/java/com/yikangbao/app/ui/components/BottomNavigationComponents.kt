package com.yikangbao.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.yikangbao.app.ui.navigation.Screen
import com.yikangbao.app.ui.theme.AppleCardCornerRadius
import com.yikangbao.app.ui.theme.HealthGreen
import com.yikangbao.app.ui.theme.Surface
import com.yikangbao.app.ui.theme.TextQuaternary
import com.yikangbao.app.ui.theme.TextTertiary
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut

@Composable
fun AppleBottomNavigation(
    navController: NavController,
    currentScreen: String
) {
    val navItems = listOf(
        NavigationItem(
            route = Screen.Home.route,
            icon = Icons.Default.Home,
            label = "首页"
        ),
        NavigationItem(
            route = Screen.Chat.route,
            icon = Icons.Default.Chat,
            label = "对话"
        ),
        NavigationItem(
            route = Screen.Records.route,
            icon = Icons.Default.Description,
            label = "档案"
        ),
        NavigationItem(
            route = Screen.Reminder.route,
            icon = Icons.Default.Notifications,
            label = "提醒"
        ),
        NavigationItem(
            route = Screen.Profile.route,
            icon = Icons.Default.Person,
            label = "我的"
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(
                    color = Surface,
                    shape = RoundedCornerShape(AppleCardCornerRadius)
                )
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(AppleCardCornerRadius),
                    spotColor = Color(0x1A000000)
                ),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { item ->
                val isSelected = currentScreen == item.route
                AppleNavItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = {
                        if (!isSelected) {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AppleNavItem(
    item: NavigationItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(
                    bounded = false,
                    color = Color(0x1F000000)
                ),
                onClick = onClick
            )
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn(animationSpec = tween(150)),
                exit = fadeOut(animationSpec = tween(150))
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = HealthGreen.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {}
            }

            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = if (isSelected) HealthGreen else TextTertiary,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = item.label,
            fontSize = 10.sp,
            color = if (isSelected) HealthGreen else TextQuaternary,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

data class NavigationItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)