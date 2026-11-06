package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.*
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.components.progress.CircularProgressRing
import com.simats.selfora.ui.components.progress.SelforaLinearProgress
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyzeAndAdaptScreen(
    childId: Long = 1L,
    onNavigateToPromptFading: (Long) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf("Dressing") }
    var activityId by remember { mutableStateOf(1L) }
    var dashboardData by remember { mutableStateOf<AnalyzeAndAdaptDashboardResponse?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Custom Adaptation Form state
    var showCustomDialog by remember { mutableStateOf(false) }
    var targetStepText by remember { mutableStateOf("Step 4: Sleeve Placement") }
    var currentPromptText by remember { mutableStateOf("Verbal Prompt (L3)") }
    var targetPromptText by remember { mutableStateOf("Visual Prompt (L1)") }
    var clinicalNotesText by remember { mutableStateOf("Child performed step independently. Progressing prompt fading as per clinical evaluation.") }
    var reviewDateText by remember { mutableStateOf("2026-10-15") }
    var planStatus by remember { mutableStateOf("Approved") }
    var isSavingPlan by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var loadErrorMessage by remember { mutableStateOf<String?>(null) }

    val adlCategories = listOf(
        "Dressing" to 1L,
        "Eating" to 13L,
        "Grooming" to 5L,
        "Shoes & Socks" to 9L
    )

    fun loadDashboard(actId: Long) {
        isLoading = true
        loadErrorMessage = null
        scope.launch {
            try {
                val res = ApiClient.apiService.getAnalyzeAndAdaptDashboard(childId, actId)
                if (res.isSuccessful && res.body() != null) {
                    dashboardData = res.body()
                } else {
                    dashboardData = null
                    val code = res.code()
                    loadErrorMessage = if (code == 403) "Access Denied: You are not authorized to view this child's analytics." else "Unable to load adaptive dashboard (HTTP $code)"
                }
            } catch (e: Exception) {
                dashboardData = null
                loadErrorMessage = "Network error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(activityId) {
        loadDashboard(activityId)
    }

    val childName = dashboardData?.childName ?: "Child #$childId"

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Clinical Analyze & Adapt",
                subtitle = "Child: $childName (#$childId)",
                accentColor = SelforaPrimary
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 1. CHILD & ACTIVITY SUMMARY HEADER
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ANALYZE & ADAPT DASHBOARD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(childName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        Text("Child ID: #$childId • Selected ADL: $selectedCategory", fontSize = 12.sp, color = SelforaTextSecondary)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if ((dashboardData?.overallTrend ?: "IMPROVING") == "IMPROVING") SelforaSuccess.copy(alpha = 0.15f) else SelforaWarning.copy(alpha = 0.15f)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if ((dashboardData?.overallTrend ?: "IMPROVING") == "IMPROVING") Icons.Default.TrendingUp else Icons.Default.TrendingFlat,
                                contentDescription = null,
                                tint = if ((dashboardData?.overallTrend ?: "IMPROVING") == "IMPROVING") SelforaSuccess else SelforaWarning,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                dashboardData?.overallTrend ?: "IMPROVING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if ((dashboardData?.overallTrend ?: "IMPROVING") == "IMPROVING") SelforaSuccess else SelforaWarning
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SelforaBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                // Activity Category Filter Selector
                Text("ADL ACTIVITY FILTERS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    adlCategories.forEach { (catName, catActId) ->
                        val isSelected = selectedCategory == catName
                        val iconStr = when (catName) {
                            "Dressing" -> "👕"
                            "Eating" -> "🥄"
                            "Grooming" -> "🧼"
                            else -> "👟"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) SelforaPrimary else Color.White)
                                .border(1.dp, if (isSelected) SelforaPrimary else SelforaBorder, RoundedCornerShape(16.dp))
                                .clickable {
                                    selectedCategory = catName
                                    activityId = catActId
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(iconStr, fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(catName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else SelforaTextPrimary)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SelforaPrimary)
                }
            } else if (loadErrorMessage != null || dashboardData == null) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp).fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = SelforaPrimary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Adaptive Analytics Unavailable",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SelforaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            loadErrorMessage ?: "Unable to fetch Analyze & Adapt data for this child.",
                            color = SelforaTextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { loadDashboard(activityId) },
                            colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                val data = dashboardData!!

                // 2. OVERALL PERFORMANCE CARD
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("OVERALL PERFORMANCE SUMMARY ($selectedCategory)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text("Current Independence", fontSize = 11.sp, color = SelforaTextSecondary)
                                    Text("${data.currentIndependencePercentage.toInt()}%", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = SelforaPrimary)
                                }
                                Column {
                                    Text("Previous Session", fontSize = 11.sp, color = SelforaTextSecondary)
                                    Text("${data.previousIndependencePercentage.toInt()}%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                                }
                                Column {
                                    Text("Recorded", fontSize = 11.sp, color = SelforaTextSecondary)
                                    Text("${data.totalRecordedSessions} sessions", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Current vs Previous Comparison Progress Bar
                            SelforaLinearProgress(
                                progress = (data.currentIndependencePercentage / 100.0).toFloat(),
                                color = SelforaPrimary,
                                height = 7.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        CircularProgressRing(
                            progress = (data.currentIndependencePercentage / 100.0).toFloat(),
                            size = 64.dp,
                            strokeWidth = 6.dp,
                            ringColor = SelforaPrimary,
                            centerText = "${data.currentIndependencePercentage.toInt()}%",
                            subText = "Overall"
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Latest Evaluation: ${data.latestAssessmentDate} • Previous: ${data.previousAssessmentDate}", fontSize = 10.sp, color = SelforaTextMuted)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. 4 ADL PERFORMANCE CARDS GRID
                Text("SUPPORTED ADL CATEGORY METRICS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    data.activityCards.forEach { card ->
                        val iconEmoji = when (card.categoryName) {
                            "Dressing" -> "👕"
                            "Eating" -> "🥄"
                            "Grooming" -> "🧼"
                            else -> "👟"
                        }
                        val categoryColor = when (card.categoryName) {
                            "Dressing" -> Color(0xFF2563EB)
                            "Eating" -> Color(0xFF7C3AED)
                            "Grooming" -> Color(0xFF10B981)
                            else -> Color(0xFFF59E0B)
                        }

                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(iconEmoji, fontSize = 20.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(card.categoryName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                        Text("${card.sessionsCount} Recorded Sessions", fontSize = 11.sp, color = SelforaTextSecondary)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${card.currentIndependencePct}%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = categoryColor)
                                    Surface(shape = RoundedCornerShape(8.dp), color = SelforaSuccess.copy(alpha = 0.15f)) {
                                        Text(card.trend, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = SelforaSuccess, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Modern Animated Progress Indicator
                            SelforaLinearProgress(
                                progress = (card.currentIndependencePct / 100.0).toFloat(),
                                color = categoryColor,
                                height = 7.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. DIFFICULT STEPS & INDEPENDENT STEPS
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Difficult Steps
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = SelforaOrangeWarning, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DIFFICULT STEPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaOrangeWarning)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        if (data.difficultSteps.isEmpty()) {
                            Text("No difficult steps identified for this activity.", fontSize = 11.sp, color = SelforaTextSecondary)
                        } else {
                            data.difficultSteps.forEach { d ->
                                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                    Text("Step ${d.stepNumber}: ${d.stepTitle}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                    Text("Level: ${d.mainAssistanceLevel}", fontSize = 10.sp, color = SelforaOrangeWarning)
                                    Text(d.reasonDescription, fontSize = 10.sp, color = SelforaTextSecondary)
                                }
                            }
                        }
                    }

                    // Independent Steps
                    GlassCard(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SelforaSuccess, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("INDEPENDENT STEPS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaSuccess)
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        if (data.independentSteps.isEmpty()) {
                            Text("No consistently independent steps yet.", fontSize = 11.sp, color = SelforaTextSecondary)
                        } else {
                            data.independentSteps.forEach { ind ->
                                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                                    Text("Step ${ind.stepNumber}: ${ind.stepTitle}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                    Text("Independent in ${ind.consecutiveIndependentSessions} sessions", fontSize = 10.sp, color = SelforaSuccess)
                                    Text(ind.generalizationReadiness, fontSize = 10.sp, color = SelforaTextSecondary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. SESSION COMPARISON CARD
                data.sessionComparison?.let { comp ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("SESSION-TO-SESSION COMPARISON", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Text("Comparing ${comp.currentSessionDate} (Latest) vs ${comp.previousSessionDate} (Previous)", fontSize = 11.sp, color = SelforaTextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Latest Session", fontSize = 11.sp, color = SelforaTextSecondary)
                                Text("${comp.currentIndependencePercentage}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            }
                            Column {
                                Text("Previous Session", fontSize = 11.sp, color = SelforaTextSecondary)
                                Text("${comp.previousIndependencePercentage}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                            }
                            Column {
                                Text("Net Change", fontSize = 11.sp, color = SelforaTextSecondary)
                                val diff = comp.currentIndependencePercentage - comp.previousIndependencePercentage
                                Text(if (diff >= 0) "+${Math.round(diff * 10.0) / 10.0}%" else "${Math.round(diff * 10.0) / 10.0}%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = if (diff >= 0) SelforaSuccess else SelforaError)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Visual Comparison Indicator
                        SelforaLinearProgress(
                            progress = (comp.currentIndependencePercentage / 100.0).toFloat(),
                            color = SelforaPrimary,
                            height = 6.dp,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        val impStr = if (comp.improvedSteps.isNotEmpty()) comp.improvedSteps.joinToString(", ") else "None"
                        val unchStr = if (comp.unchangedSteps.isNotEmpty()) comp.unchangedSteps.joinToString(", ") else "None"
                        Text("• Improved Steps: $impStr", fontSize = 11.sp, color = SelforaSuccess)
                        Text("• Unchanged Steps: $unchStr", fontSize = 11.sp, color = SelforaTextSecondary)
                        if (comp.regressedSteps.isNotEmpty()) {
                            Text("• Regressed Steps: ${comp.regressedSteps.joinToString(", ")}", fontSize = 11.sp, color = SelforaError)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 6. FACTUAL CLINICAL OBSERVATIONS
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Insights, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("FACTUAL CLINICAL OBSERVATIONS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    }
                    Text("Calculated directly from actual persisted session records", fontSize = 11.sp, color = SelforaTextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    if (data.factualObservations.isEmpty()) {
                        Text("No clinical observations recorded yet.", fontSize = 12.sp, color = SelforaTextSecondary)
                    } else {
                        data.factualObservations.forEach { obs ->
                            Row(modifier = Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
                                Text("• ", fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                Text(obs, fontSize = 12.sp, color = SelforaTextPrimary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 7. RULE-BASED RECOMMENDATION ENGINE
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = SelforaSecondary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("CLINICAL RECOMMENDATIONS (RULE-BASED)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaSecondary)
                    }
                    Text("System-generated suggestions for therapist consideration (Explainable deterministic rules)", fontSize = 11.sp, color = SelforaTextSecondary)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (data.recommendations.isEmpty()) {
                        Text("No adaptation recommendations at this time. Performance is stable.", fontSize = 12.sp, color = SelforaTextSecondary)
                    } else {
                        data.recommendations.forEach { rec ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = SelforaSurface.copy(alpha = 0.6f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SelforaBorder),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Trigger: ${rec.triggeringEvidence}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                    Text("Relevant Activity / Step: ${rec.activityTitle} • ${rec.stepTitle} (${rec.sessionDates})", fontSize = 10.sp, color = SelforaTextSecondary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Suggested Action: ${rec.suggestedTherapistAction}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 8. THERAPIST-CONTROLLED ADAPTATION PLAN WORKFLOW
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("THERAPIST ADAPTATION PLAN WORKFLOW", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Text("Full therapist control over clinical goals & prompt fading", fontSize = 11.sp, color = SelforaTextSecondary)
                        }
                        Surface(shape = RoundedCornerShape(10.dp), color = SelforaSuccess.copy(alpha = 0.15f)) {
                            Text(planStatus, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaSuccess, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Active Adaptation Goal for $selectedCategory:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    Text("Target Step: $targetStepText", fontSize = 12.sp, color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
                    Text("Current Assistance: $currentPromptText ➔ Target: $targetPromptText", fontSize = 12.sp, color = SelforaTextSecondary)
                    Text("Review Date: $reviewDateText", fontSize = 11.sp, color = SelforaTextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Clinical Notes: \"$clinicalNotesText\"", fontSize = 12.sp, color = SelforaTextPrimary, fontWeight = FontWeight.Medium)

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassButton(
                            text = "Accept Suggestion",
                            onClick = {
                                planStatus = "Approved"
                                statusMessage = "Adaptation plan approved by therapist."
                            },
                            icon = Icons.Default.Check,
                            modifier = Modifier.weight(1f)
                        )
                        GlassOutlinedButton(
                            text = "Modify Plan",
                            onClick = { showCustomDialog = true },
                            icon = Icons.Default.Edit,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassOutlinedButton(
                            text = "Reject",
                            onClick = {
                                planStatus = "Rejected"
                                statusMessage = "Adaptation plan rejected."
                            },
                            accentColor = SelforaError,
                            icon = Icons.Default.Close,
                            modifier = Modifier.weight(1f)
                        )

                        GlassOutlinedButton(
                            text = "Prompt Fading Hub →",
                            onClick = { onNavigateToPromptFading(childId) },
                            icon = Icons.Default.TrendingDown,
                            modifier = Modifier.weight(1.2f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = SelforaPurpleAccent.copy(alpha = 0.08f), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "🛡️ CLINICAL SAFEGUARD: The system never automatically changes clinical goals or prompt levels without therapist approval.",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaSecondary,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }

    // Modify/Create Custom Adaptation Plan Dialog
    if (showCustomDialog) {
        val savePlanAction = {
            planStatus = "Active"
            showCustomDialog = false
        }

        AlertDialog(
            modifier = Modifier.imePadding(),
            onDismissRequest = { showCustomDialog = false },
            title = {
                Text("Modify / Custom Adaptation Plan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassTextField(
                        value = targetStepText,
                        onValueChange = { targetStepText = it },
                        label = "Target Task Step",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )
                    GlassTextField(
                        value = currentPromptText,
                        onValueChange = { currentPromptText = it },
                        label = "Current Assistance Level",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )
                    GlassTextField(
                        value = targetPromptText,
                        onValueChange = { targetPromptText = it },
                        label = "Target Fading Prompt Level",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )
                    GlassTextField(
                        value = clinicalNotesText,
                        onValueChange = { clinicalNotesText = it },
                        label = "Clinical Rationale & Notes",
                        singleLine = false,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier.fillMaxWidth()
                    )
                    GlassTextField(
                        value = reviewDateText,
                        onValueChange = { reviewDateText = it },
                        label = "Review Date (YYYY-MM-DD)",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { savePlanAction() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = savePlanAction,
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                ) {
                    Text("Save Approved Plan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
