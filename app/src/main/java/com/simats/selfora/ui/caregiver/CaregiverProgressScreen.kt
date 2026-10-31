package com.simats.selfora.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.components.progress.*
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverProgressScreen(
    onNavigateToHistory: () -> Unit,
    onNavigateToTab: (String) -> Unit
) {
    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            GlassTopBar(
                title = "Progress",
                subtitle = "Weekly Overview & Home Practice",
                accentColor = SelforaPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Weekly Date Selector
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Week", tint = SelforaPrimary)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Weekly Overview", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextSecondary)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("Active Practice Week", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    }

                    IconButton(onClick = {}) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Week", tint = SelforaPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Overall Home Independence Circular Progress Ring
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "CHILD INDEPENDENCE OVERVIEW",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaTextSecondary,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    CircularProgressRing(
                        progress = 0.68f,
                        size = 140.dp,
                        strokeWidth = 11.dp,
                        ringColor = SelforaPrimary,
                        showPercentageText = true,
                        subText = "Independence"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Consistent weekly improvement across active ADL routines",
                        fontSize = 12.sp,
                        color = SelforaTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ADL Categories Summary
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ADL CATEGORY MASTERY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaTextSecondary,
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    SelforaAdlCategoryGrid(
                        dressingPct = 60,
                        eatingPct = 70,
                        groomingPct = 68,
                        shoesPct = 75
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Specific Activity Breakdown
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "HOME PRACTICE ACTIVITIES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaTextSecondary,
                        letterSpacing = 0.5.sp
                    )

                    CaregiverActivityProgressItem(
                        activityName = "Tooth Brushing",
                        category = "Grooming",
                        stepsCompletedText = "(12 / 16 steps)",
                        percentage = 68
                    )

                    CaregiverActivityProgressItem(
                        activityName = "Wearing T-Shirt",
                        category = "Dressing",
                        stepsCompletedText = "(9 / 15 steps)",
                        percentage = 60
                    )

                    CaregiverActivityProgressItem(
                        activityName = "Eating with Spoon",
                        category = "Eating",
                        stepsCompletedText = "(14 / 20 steps)",
                        percentage = 70
                    )

                    CaregiverActivityProgressItem(
                        activityName = "Velcro Shoes",
                        category = "Shoes & Socks",
                        stepsCompletedText = "(6 / 8 steps)",
                        percentage = 75
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            GlassButton(
                text = "View Practice Session History",
                onClick = onNavigateToHistory,
                icon = Icons.Default.History,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
fun CaregiverActivityProgressItem(
    activityName: String,
    category: String,
    stepsCompletedText: String,
    percentage: Int
) {
    val (color, icon, _) = getADLCategoryVisuals(category)
    val safePct = percentage.coerceIn(0, 100)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category,
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = activityName,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stepsCompletedText,
                        fontSize = 10.5.sp,
                        color = SelforaTextSecondary
                    )
                }
            }

            Text(
                text = "$safePct%",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        SelforaLinearProgress(
            percentage = safePct,
            color = color,
            height = 7.dp
        )
    }
}
