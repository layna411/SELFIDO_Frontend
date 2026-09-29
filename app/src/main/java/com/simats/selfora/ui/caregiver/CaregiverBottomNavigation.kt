package com.simats.selfora.ui.caregiver

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.theme.*

sealed class CaregiverTab(val route: String, val title: String, val icon: ImageVector) {
    object Home : CaregiverTab("caregiver_dashboard", "Home", Icons.Default.Home)
    object Programme : CaregiverTab("home_programme", "Programme", Icons.Default.Assignment)
    object Progress : CaregiverTab("caregiver_progress", "Progress", Icons.Default.BarChart)
    object Messages : CaregiverTab("caregiver_messages", "Messages", Icons.Default.Message)
}

@Composable
fun CaregiverBottomNavigation(
    currentRoute: String,
    onTabSelected: (String) -> Unit
) {
    val tabs = listOf(
        CaregiverTab.Home,
        CaregiverTab.Programme,
        CaregiverTab.Progress,
        CaregiverTab.Messages
    )

    NavigationBar(
        containerColor = SelforaSurface,
        tonalElevation = 8.dp
    ) {
        tabs.forEach { tab ->
            val isSelected = currentRoute.startsWith(tab.route)
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab.route) },
                icon = {
                    Icon(
                        imageVector = tab.icon,
                        contentDescription = tab.title,
                        tint = if (isSelected) SelforaPrimary else SelforaTextSecondary
                    )
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) SelforaPrimary else SelforaTextSecondary
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = SelforaPrimary.copy(alpha = 0.12f)
                )
            )
        }
    }
}
