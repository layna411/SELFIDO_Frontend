package com.simats.selfora.ui.child

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.ChildActivitySummaryDto
import com.simats.selfora.data.model.ChildDashboardResponse
import com.simats.selfora.data.model.ChildRewardDto
import com.simats.selfora.ui.components.avatar.ChildAvatarImage
import com.simats.selfora.ui.components.glass.GlassCard
import com.simats.selfora.ui.components.glass.GlassIconButton
import com.simats.selfora.ui.components.progress.ChildRewardProgress
import com.simats.selfora.ui.theme.*
import com.simats.selfora.utils.LanguageManager
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

    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isTamil = currentLanguage == "ta"

    var dashboardData by remember { mutableStateOf<ChildDashboardResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val activeChildId = remember { SessionManager.getActiveChildId() }

    LaunchedEffect(currentLanguage) {
        ttsManager.setLanguage(if (isTamil) "ta" else "en")
    }

    fun loadChildDashboard() {
        if (activeChildId <= 0) {
            errorMessage = if (isTamil) "குழந்தை தேர்ந்தெடுக்கப்படவில்லை." else "No active child selected."
            isLoading = false
            return
        }

        isLoading = true
        errorMessage = null
        dashboardData = null // Clear state when switching/reloading children

        scope.launch {
            try {
                val res = ApiClient.apiService.getChildModeDashboard(activeChildId)
                if (res.isSuccessful && res.body() != null) {
                    dashboardData = res.body()
                    ttsManager.setLanguage(if (isTamil) "ta" else "en")
                    dashboardData?.preferredName?.let { pName ->
                        if (isTamil) {
                            ttsManager.speak("வணக்கம், $pName! வாருங்கள், பயிற்சி செய்யலாம்!")
                        } else {
                            ttsManager.speak("Welcome back, $pName! Let's practise today!")
                        }
                    }
                } else {
                    errorMessage = if (isTamil) "அனுமதி மறுக்கப்பட்டது அல்லது குழந்தை கிடைக்கவில்லை (${res.code()})" else "Access denied or child not found (${res.code()})"
                }
            } catch (e: Exception) {
                errorMessage = "Network error: ${e.localizedMessage ?: "Unable to connect"}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(activeChildId) {
        loadChildDashboard()
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

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(SelforaBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = SelforaPrimary)
        }
    } else if (errorMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize().background(SelforaBackground).padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 20.dp,
                backgroundColor = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔒", fontSize = 42.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        if (isTamil) "குழந்தை பயன்முறையைத் திறக்க முடியவில்லை" else "Unable to Load Child Mode",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SelforaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        errorMessage!!,
                        fontSize = 13.sp,
                        color = SelforaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { loadChildDashboard() },
                        colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                    ) {
                        Text(if (isTamil) "மீண்டும் முயற்சி செய்" else "Retry", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    } else {
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
        // Header (Greeting, Avatar, Language Toggle, Settings & Adult Exit)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                ChildAvatarImage(
                    avatarId = if (isGirl) "ananya_girl" else "aarav_boy",
                    gender = if (isGirl) "GIRL" else "BOY",
                    size = 56.dp,
                    animateWave = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isTamil) "வணக்கம், $childName! ✨" else "Hi, $childName! ✨",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = primaryThemeColor
                    )
                    Text(
                        text = if (isTamil) "இன்று மகிழ்ச்சியாகக் கற்போமா?" else "Ready for fun learning?",
                        fontSize = 12.sp,
                        color = SelforaTextSecondary
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Immediate Language Switcher Toggle Pill
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (isTamil) Color(0xFFFEF3C7) else Color(0xFFEFF6FF),
                    border = BorderStroke(1.2.dp, if (isTamil) Color(0xFFF59E0B) else SelforaPrimary),
                    modifier = Modifier.clickable {
                        LanguageManager.toggleLanguage()
                    }
                ) {
                    Text(
                        text = if (isTamil) "🇮🇳 தமிழ்" else "🇬🇧 English",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isTamil) Color(0xFFB45309) else SelforaPrimary,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                    )
                }

                GlassIconButton(
                    icon = Icons.Default.Settings,
                    onClick = onOpenSettings,
                    tint = SelforaTextSecondary,
                    size = 38.dp,
                    iconSize = 18.dp
                )
                GlassIconButton(
                    icon = Icons.Default.ExitToApp,
                    onClick = { showExitDialog = true },
                    tint = SelforaError,
                    size = 38.dp,
                    iconSize = 18.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Total Earned Rewards Banner with Interactive Animated Progress
        ChildRewardProgress(
            starsEarned = dashboardData?.totalStars ?: 0,
            totalStars = 5,
            currentStreak = dashboardData?.currentStreak ?: 0,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Badges & Trophies
        val allBadges = dashboardData?.allBadges ?: emptyList()
        if (allBadges.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isTamil) "என் பதக்கங்கள் 🏆" else "My Badges & Trophies 🏆",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SelforaTextPrimary
                )
                val unlockedCount = allBadges.count { it.isUnlocked }
                Text(
                    text = if (isTamil) "$unlockedCount / ${allBadges.size} திறக்கப்பட்டது" else "$unlockedCount of ${allBadges.size} Unlocked",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelforaPrimary
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                allBadges.forEach { badge ->
                    ChildBadgeCard(badge = badge, isTamil = isTamil)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Section A: Today's Assigned Home Programme Activities
        Text(
            text = if (isTamil) "இன்றைய பயிற்சிகள் 🎯" else "Today's Assigned Activities 🎯",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = SelforaTextPrimary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        val assigned = dashboardData?.assignedActivities ?: emptyList()
        if (assigned.isNotEmpty()) {
            assigned.forEach { act ->
                AssignedActivityCard(
                    act = act,
                    primaryColor = primaryThemeColor,
                    isTamil = isTamil,
                    onClick = { onSelectActivity(act.activityId) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        } else {
            GlassCard(modifier = Modifier.fillMaxWidth(), cornerRadius = 18.dp, backgroundColor = Color.White) {
                Text(
                    text = if (isTamil) "இன்று பயிற்சிகள் எதுவும் ஒதுக்கப்படவில்லை." else "No home programme activities assigned for today.",
                    fontSize = 13.sp,
                    color = SelforaTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section C: Category Filter Tabs (Approved ADL Scope: Dressing, Upper/Lower Body, Footwear, Eating)
        Text(
            text = if (isTamil) "என் பயிற்சிகள் 📚" else "My ADL Activities 📚",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = SelforaTextPrimary,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        val categoryTabs = listOf(
            "ALL" to if (isTamil) "அனைத்தும்" else "ALL",
            "Dressing" to if (isTamil) "ஆடை அணிதல்" else "Dressing",
            "Upper Body" to if (isTamil) "மேல் உடல்" else "Upper Body",
            "Lower Body" to if (isTamil) "கீழ் உடல்" else "Lower Body",
            "Shoes & Socks" to if (isTamil) "காலணிகள்" else "Shoes & Socks",
            "Eating" to if (isTamil) "உணவு" else "Eating"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryTabs.forEach { (catKey, catLabel) ->
                FilterChip(
                    selected = selectedCategory == catKey,
                    onClick = { selectedCategory = catKey },
                    label = { Text(catLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Category Filtered Activity List
        val allActs = dashboardData?.allActivities ?: emptyList()
        val filteredActs = when (selectedCategory) {
            "ALL" -> allActs
            "Dressing" -> allActs.filter {
                it.categoryName.contains("Dressing", ignoreCase = true) ||
                it.subCategory?.contains("Dressing", ignoreCase = true) == true
            }
            "Upper Body" -> allActs.filter {
                it.categoryName.contains("Upper", ignoreCase = true) ||
                it.subCategory?.contains("Upper", ignoreCase = true) == true
            }
            "Lower Body" -> allActs.filter {
                it.categoryName.contains("Lower", ignoreCase = true) ||
                it.subCategory?.contains("Lower", ignoreCase = true) == true
            }
            "Shoes & Socks" -> allActs.filter {
                it.categoryName.contains("Shoe", ignoreCase = true) ||
                it.categoryName.contains("Sock", ignoreCase = true) ||
                it.categoryName.contains("Footwear", ignoreCase = true) ||
                it.title.contains("Shoe", ignoreCase = true) ||
                it.title.contains("Sock", ignoreCase = true)
            }
            "Eating" -> allActs.filter {
                it.categoryName.contains("Eating", ignoreCase = true) ||
                it.title.contains("Eating", ignoreCase = true)
            }
            else -> allActs
        }

        filteredActs.forEach { act ->
            CategoryActivityCard(
                act = act,
                isTamil = isTamil,
                onClick = { onSelectActivity(act.activityId) }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}
}

@Composable
fun ChildBadgeCard(badge: ChildRewardDto, isTamil: Boolean = false) {
    Surface(
        modifier = Modifier
            .width(135.dp)
            .height(155.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (badge.isUnlocked) Color(0xFFFFFBEB) else Color(0xFFF8FAFC),
        border = BorderStroke(
            width = if (badge.isUnlocked) 1.5.dp else 1.dp,
            color = if (badge.isUnlocked) Color(0xFFF59E0B) else Color(0xFFE2E8F0)
        ),
        shadowElevation = if (badge.isUnlocked) 3.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (badge.isUnlocked) (badge.badgeIcon ?: "⭐") else "🔒",
                fontSize = 32.sp
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = badge.badgeTitle,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (badge.isUnlocked) Color(0xFF92400E) else Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val defaultDesc = if (badge.isUnlocked) {
                    if (isTamil) "முடிக்கப்பட்டது!" else "Completed!"
                } else {
                    if (isTamil) "திறக்க பயிற்சி செய்" else "Practice to unlock"
                }
                Text(
                    text = badge.description.ifBlank { defaultDesc },
                    fontSize = 10.sp,
                    color = if (badge.isUnlocked) Color(0xFFB45309) else Color(0xFF94A3B8),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (badge.isUnlocked) Color(0xFFFEF3C7) else Color(0xFFF1F5F9)
            ) {
                val badgeStatus = if (badge.isUnlocked) {
                    if (isTamil) "திறக்கப்பட்டது! 🎉" else "Unlocked! 🎉"
                } else {
                    if (isTamil) "பூட்டப்பட்டுள்ளது" else "Locked"
                }
                Text(
                    text = badgeStatus,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (badge.isUnlocked) Color(0xFFD97706) else Color(0xFF94A3B8),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun AssignedActivityCard(
    act: ChildActivitySummaryDto,
    primaryColor: Color,
    isTamil: Boolean = false,
    onClick: () -> Unit
) {
    val displayTitle = if (isTamil && !act.titleTa.isNullOrBlank()) act.titleTa else act.title

    val ctaText = if (isTamil) {
        when {
            act.isCompleted -> "மீண்டும் செய்"
            act.completedSteps > 0 -> "படி ${act.completedSteps + 1}"
            else -> "தொடங்கு"
        }
    } else {
        when {
            act.isCompleted -> "Replay"
            act.completedSteps > 0 -> "Step ${act.completedSteps + 1}"
            else -> "Start"
        }
    }
    val ctaColor = when {
        act.isCompleted -> SelforaPrimary
        act.completedSteps > 0 -> Color(0xFFEA580C)
        else -> ChildGreenPlay
    }

    val subtitleText = if (isTamil) {
        if (act.isCompleted) "முடிக்கப்பட்டது ⭐ • மீண்டும் செய்யலாம்"
        else if (act.completedSteps > 0) "படி ${act.completedSteps} / ${act.totalSteps} முடிந்தது"
        else "${act.totalSteps} எளிய படிகள்"
    } else {
        if (act.isCompleted) "Completed ⭐ • Replay anytime"
        else if (act.completedSteps > 0) "Step ${act.completedSteps} of ${act.totalSteps} Completed"
        else "${act.totalSteps} Simple Steps"
    }

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
                    Text(displayTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                    Text(
                        text = subtitleText,
                        fontSize = 12.sp,
                        color = if (act.completedSteps > 0 && !act.isCompleted) Color(0xFFEA580C) else SelforaTextSecondary
                    )
                }
            }
            Button(
                onClick = onClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ctaColor)
            ) {
                Text(ctaText, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun CategoryActivityCard(
    act: ChildActivitySummaryDto,
    isTamil: Boolean = false,
    onClick: () -> Unit
) {
    val displayTitle = if (isTamil && !act.titleTa.isNullOrBlank()) act.titleTa else act.title

    val ctaText = if (isTamil) {
        when {
            act.isCompleted -> "மீண்டும் செய்"
            act.completedSteps > 0 -> "தொடரவும்"
            else -> "தொடங்கு"
        }
    } else {
        when {
            act.isCompleted -> "Replay"
            act.completedSteps > 0 -> "Continue"
            else -> "Start"
        }
    }
    val ctaBg = when {
        act.isCompleted -> SelforaPrimary.copy(alpha = 0.12f)
        act.completedSteps > 0 -> Color(0xFFFFF7ED)
        else -> ChildGreenPlay.copy(alpha = 0.12f)
    }
    val ctaColor = when {
        act.isCompleted -> SelforaPrimary
        act.completedSteps > 0 -> Color(0xFFEA580C)
        else -> ChildGreenPlay
    }

    val subtitleText = if (isTamil) {
        if (act.isCompleted) "முடிக்கப்பட்டது ⭐ • மீண்டும் செய்"
        else if (act.completedSteps > 0) "படி ${act.completedSteps + 1} / ${act.totalSteps}"
        else "${act.totalSteps} எளிய படிகள்"
    } else {
        if (act.isCompleted) "Mastered ⭐ • Replay"
        else if (act.completedSteps > 0) "Step ${act.completedSteps + 1} of ${act.totalSteps}"
        else "${act.categoryName} • ${act.totalSteps} Steps"
    }

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
                    Text(displayTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextPrimary)
                    Text(
                        text = subtitleText,
                        fontSize = 12.sp,
                        color = if (act.completedSteps > 0 && !act.isCompleted) Color(0xFFEA580C) else SelforaTextSecondary
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ctaBg,
                modifier = Modifier.clickable(onClick = onClick)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(ctaText, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ctaColor)
                    Icon(
                        imageVector = if (act.isCompleted) Icons.Default.Refresh else Icons.Default.PlayArrow,
                        contentDescription = ctaText,
                        tint = ctaColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
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
