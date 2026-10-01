package com.simats.selfora.ui.caregiver

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.theme.*

sealed class CaregiverTab(val route: String, val title: String, val icon: ImageVector) {
    object Home : CaregiverTab("caregiver_dashboard", "Home", Icons.Default.Home)
    object Programme : CaregiverTab("home_programme", "Programme", Icons.Default.Assignment)
    object Progress : CaregiverTab("caregiver_progress", "Progress", Icons.Default.BarChart)
    object Messages : CaregiverTab("caregiver_messages", "Messages", Icons.Default.Chat)
    object Profile : CaregiverTab("caregiver_profile", "Profile", Icons.Default.Person)
}

@Composable
fun CaregiverBottomNavigation(
    currentRoute: String,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        CaregiverTab.Home,
        CaregiverTab.Programme,
        CaregiverTab.Progress,
        CaregiverTab.Messages,
        CaregiverTab.Profile
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(26.dp),
                    spotColor = Color(0x227C3AED)
                ),
            shape = RoundedCornerShape(26.dp),
            color = Color.White.copy(alpha = 0.96f),
            border = BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color.White,
                        SelforaSecondary.copy(alpha = 0.20f),
                        Color.White
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { tab ->
                    val isSelected = when (tab) {
                        CaregiverTab.Home -> currentRoute == "caregiver_dashboard"
                        CaregiverTab.Programme -> currentRoute.startsWith("home_programme") || currentRoute.startsWith("home_practice")
                        CaregiverTab.Progress -> currentRoute.startsWith("caregiver_progress") || currentRoute.startsWith("caregiver_history")
                        CaregiverTab.Messages -> currentRoute.startsWith("caregiver_messages")
                        CaregiverTab.Profile -> currentRoute.startsWith("caregiver_profile") || currentRoute.startsWith("caregiver_notifications")
                        else -> false
                    }

                    val tabBgColor by animateColorAsState(
                        targetValue = if (isSelected) SelforaSecondary.copy(alpha = 0.12f) else Color.Transparent,
                        animationSpec = tween(250)
                    )
                    val iconScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.10f else 1.0f,
                        animationSpec = tween(200)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(tabBgColor)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onTabSelected(tab.route) }
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isSelected) {
                                Surface(
                                    modifier = Modifier.size(30.dp),
                                    shape = CircleShape,
                                    color = SelforaSecondary
                                ) {}
                            }
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                tint = if (isSelected) Color.White else SelforaTextSecondary,
                                modifier = Modifier
                                    .size(18.dp)
                                    .scale(iconScale)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.title,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SelforaSecondary else SelforaTextSecondary,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
