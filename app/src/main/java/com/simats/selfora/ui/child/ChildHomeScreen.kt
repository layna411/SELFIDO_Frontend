package com.simats.selfora.ui.child

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.ChildActivitySummaryDto
import com.simats.selfora.data.model.ChildDashboardResponse
import com.simats.selfora.ui.components.avatar.ChildAvatarImage
import com.simats.selfora.ui.components.glass.GlassCard
import com.simats.selfora.ui.components.glass.GlassIconButton
import com.simats.selfora.ui.theme.*
import com.simats.selfora.utils.TtsManager
import kotlinx.coroutines.launch

@Composable
fun ChildHomeScreen(
    onSelectActivity: (activityId: Long) -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    val scope = rememberCoroutineScope()

    var dashboardData by remember { mutableStateOf<ChildDashboardResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showExitDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val activeChildId = remember { SessionManager.getActiveChildId() }

    LaunchedEffect(activeChildId) {
        isLoading = true
        if (activeChildId > 0) {
            try {
                val res = ApiClient.apiService.getChildModeDashboard(activeChildId)
                if (res.isSuccessful && res.body() != null) {
                    dashboardData = res.body()
                }
            } catch (e: Exception) {
                // Ignore fallback to local mock dashboard below
            }
        }

        if (dashboardData == null) {
            val name = SessionManager.getActiveChildName().ifBlank { "Aarav" }
            val gender = SessionManager.getActiveChildGender().ifBlank { "BOY" }
            dashboardData = ChildDashboardResponse(
                childId = if (activeChildId > 0) activeChildId else 1L,
                childName = name,
                preferredName = name,
                gender = gender,
                assignedActivities = listOf(
                    ChildActivitySummaryDto(1L, "Wear a T-Shirt", "Learn to wear your T-Shirt step-by-step", null, "Dressing", 8, 3, true, false, 5),
                    ChildActivitySummaryDto(13L, "Eat with Spoon", "Hold spoon correctly and eat food", null, "Eating", 5, 5, true, true, 5)
                ),
                allActivities = listOf(
                    ChildActivitySummaryDto(1L, "Wear a T-Shirt", "Learn to wear your T-Shirt step-by-step", null, "Dressing", 8, 3, true, false, 5),
                    ChildActivitySummaryDto(2L, "Wear Frock / Dress", "Learn to wear frock step-by-step", null, "Dressing", 8, 0, false, false, 0),
                    ChildActivitySummaryDto(13L, "Eat with Spoon", "Hold spoon correctly and eat food", null, "Eating", 5, 5, true, true, 5),
                    ChildActivitySummaryDto(9L, "Wear Shoes & Socks", "Put on socks and fasten shoe straps", null, "Shoes & Socks", 6, 0, false, false, 0),
                    ChildActivitySummaryDto(14L, "Brush Teeth", "Clean your teeth with toothpaste", null, "Grooming", 5, 0, false, false, 0),
                    ChildActivitySummaryDto(15L, "Wash Hands", "Soap and rinse hands properly", null, "Grooming", 4, 0, false, false, 0)
                ),
                totalStars = 10,
                completedCount = 1
            )
        }
        isLoading = false

        dashboardData?.preferredName?.let { pName ->
            ttsManager.speak("Welcome back, $pName! Let's practise today!")
        }
    }

    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    if (showExitDialog) {
        AdultExitDialog(
            onDismiss = { showExitDialog = false },
            onConfirmExit = {
                showExitDialog = false
                onLogout()
            }
        )
    }

    val isGirl = dashboardData?.gender?.uppercase()?.contains("GIRL") == true
    val childName = dashboardData?.preferredName?.ifBlank { dashboardData?.childName } ?: "Aarav"
    val primaryThemeColor = if (isGirl) SelforaSecondary else SelforaPrimary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SelforaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header (Greeting, Avatar, Settings & Adult Exit)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ChildAvatarImage(
                    avatarId = if (isGirl) "ananya_girl" else "aarav_boy",
                    gender = if (isGirl) "GIRL" else "BOY",
                    size = 60.dp,
                    animateWave = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Hi, $childName! ✨",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = primaryThemeColor
                    )
                    Text("Ready for fun learning?", fontSize = 13.sp, color = SelforaTextSecondary)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassIconButton(
                    icon = Icons.Default.Settings,
                    onClick = onOpenSettings,
                    tint = SelforaTextSecondary,
                    size = 42.dp,
                    iconSize = 20.dp
                )
                GlassIconButton(
                    icon = Icons.Default.ExitToApp,
                    onClick = { showExitDialog = true },
                    tint = SelforaError,
                    size = 42.dp,
                    iconSize = 20.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Total Earned Rewards Banner
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 22.dp,
            backgroundColor = Color.White.copy(alpha = 0.95f),
            elevation = 6.dp
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = ChildYellowStar, modifier = Modifier.size(34.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("${dashboardData?.totalStars ?: 0} Stars Earned! 🌟", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                        Text("${dashboardData?.completedCount ?: 0} Activities Finished", fontSize = 12.sp, color = SelforaTextSecondary)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ChildGreenPlay.copy(alpha = 0.15f)
                ) {
                    Text("Great Job! 👍", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = ChildGreenPlay, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section A: Today's Assigned Home Programme Activities
        Text("Today's Assigned Activities 🎯", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SelforaTextPrimary, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))

        val assigned = dashboardData?.assignedActivities ?: emptyList()
        if (assigned.isNotEmpty()) {
            assigned.forEach { act ->
                AssignedActivityCard(act = act, primaryColor = primaryThemeColor, onClick = { onSelectActivity(act.activityId) })
                Spacer(modifier = Modifier.height(10.dp))
            }
        } else {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, backgroundColor = Color.White) {
                Text("No home programme activities assigned for today.", fontSize = 13.sp, color = SelforaTextSecondary)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section C: Category Filter Tabs
        Text("My ADL Activities 📚", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SelforaTextPrimary, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "Dressing", "Eating", "Grooming", "Shoes & Socks").forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category Filtered Activity List
        val allActs = dashboardData?.allActivities ?: emptyList()
        val filteredActs = if (selectedCategory == "ALL") allActs else allActs.filter { it.categoryName.equals(selectedCategory, ignoreCase = true) }

        filteredActs.forEach { act ->
            CategoryActivityCard(act = act, onClick = { onSelectActivity(act.activityId) })
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun AssignedActivityCard(
    act: ChildActivitySummaryDto,
    primaryColor: Color,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        backgroundColor = ChildDressingCardBg,
        borderColor = primaryColor.copy(alpha = 0.4f),
        elevation = 6.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(getCategoryIcon(act.categoryName), contentDescription = null, tint = primaryColor, modifier = Modifier.size(26.dp))
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(act.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                    Text("${act.totalSteps} Simple Steps", fontSize = 12.sp, color = SelforaTextSecondary)
                }
            }
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ChildGreenPlay)
            ) {
                Text(if (act.isCompleted) "Replay" else "Start", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun CategoryActivityCard(
    act: ChildActivitySummaryDto,
    onClick: () -> Unit
) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        backgroundColor = Color.White,
        elevation = 4.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = SelforaBackground,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(getCategoryIcon(act.categoryName), contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(act.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextPrimary)
                    Text(act.categoryName, fontSize = 12.sp, color = SelforaTextSecondary)
                }
            }
            IconButton(onClick = onClick) {
                Icon(Icons.Default.PlayCircleFilled, contentDescription = "Start", tint = ChildGreenPlay, modifier = Modifier.size(36.dp))
            }
        }
    }
}

fun getCategoryIcon(cat: String): ImageVector {
    return when {
        cat.contains("Eating", ignoreCase = true) -> Icons.Default.Restaurant
        cat.contains("Shoes", ignoreCase = true) -> Icons.Default.RollerSkating
        cat.contains("Grooming", ignoreCase = true) -> Icons.Default.Face
        else -> Icons.Default.Checkroom
    }
}
