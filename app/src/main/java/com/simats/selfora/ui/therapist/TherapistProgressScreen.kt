package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.ChildSummaryItem
import com.simats.selfora.data.model.ProgressSummaryResponse
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistProgressScreen(
    childId: Long = 1L,
    onNavigateToAnalyzeAndAdapt: (Long) -> Unit = {},
    onNavigateToPromptFading: (Long) -> Unit = {},
    onBack: () -> Unit = {}
) {
    val availableChildren = remember {
        listOf(
            ChildSummaryItem(id = 1L, name = "Arjun Kumar", age = 8, gender = "BOY", caregiverName = "Priya Kumar", caregiverRelationship = "Mother", dressingPercentage = 78, eatingPercentage = 65, shoesPercentage = 80, hasPendingAssessment = true, hasActiveHomeProgram = true),
            ChildSummaryItem(id = 2L, name = "Ananya Reddy", age = 6, gender = "GIRL", caregiverName = "Suresh Reddy", caregiverRelationship = "Father", dressingPercentage = 85, eatingPercentage = 90, shoesPercentage = 78, hasPendingAssessment = false, hasActiveHomeProgram = true),
            ChildSummaryItem(id = 3L, name = "Kavya Patel", age = 7, gender = "GIRL", caregiverName = "Meera Patel", caregiverRelationship = "Mother", dressingPercentage = 45, eatingPercentage = 50, shoesPercentage = 60, hasPendingAssessment = true, hasActiveHomeProgram = false),
            ChildSummaryItem(id = 4L, name = "Rohan Sharma", age = 9, gender = "BOY", caregiverName = "Sunita Sharma", caregiverRelationship = "Mother", dressingPercentage = 92, eatingPercentage = 88, shoesPercentage = 95, hasPendingAssessment = false, hasActiveHomeProgram = true)
        )
    }

    var selectedChild by remember {
        mutableStateOf(availableChildren.find { it.id == childId } ?: availableChildren.first())
    }
    var selectedTab by remember { mutableStateOf("Longitudinal Trends") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Longitudinal Progress Monitoring", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SelforaTextPrimary)
                        Text("Selected Child: ${selectedChild.name}", fontSize = 12.sp, color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SelforaSurface)
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // STEP 1: CHILD SELECTOR BAR
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SELECT CHILD TO MONITOR PROGRESS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableChildren.forEach { child ->
                        val isSelected = selectedChild.id == child.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .background(if (isSelected) SelforaPrimary else Color.White)
                                .border(1.dp, if (isSelected) Color.White else SelforaBorder, RoundedCornerShape(18.dp))
                                .clickable { selectedChild = child }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (child.gender == "GIRL") "👧" else "👦", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(child.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else SelforaTextPrimary)
                                    Text("Caregiver: ${child.caregiverName}", fontSize = 10.sp, color = if (isSelected) Color.White.copy(alpha = 0.85f) else SelforaTextSecondary)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Navigation Shortcuts to Stage 5 Modules
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassButton(
                    text = "Analyze & Adapt Dashboard",
                    onClick = { onNavigateToAnalyzeAndAdapt(selectedChild.id) },
                    icon = Icons.Default.Analytics,
                    modifier = Modifier.weight(1f)
                )
                GlassOutlinedButton(
                    text = "Prompt Fading Hub",
                    onClick = { onNavigateToPromptFading(selectedChild.id) },
                    icon = Icons.Default.TrendingDown,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-tabs: Longitudinal Trends vs Session Timeline vs Caregiver Practice
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Longitudinal Trends", "Session Timeline", "Caregiver Practice").forEach { tab ->
                    GlassChip(
                        text = tab,
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTab) {
                // TAB 1: LONGITUDINAL TRENDS
                "Longitudinal Trends" -> {
                    // A. Independence Trend over time
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("OVERALL INDEPENDENCE TREND OVER TIME", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Text("Recorded independence percentage across consecutive clinical sessions", fontSize = 11.sp, color = SelforaTextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Visual Trend Line / Bars Representation
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            listOf(
                                "Sep 10" to 55,
                                "Sep 15" to 62,
                                "Sep 20" to 68,
                                "Sep 25" to 70,
                                "Sep 29" to 75,
                                "Oct 02" to selectedChild.dressingPercentage
                            ).forEach { (date, pct) ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$pct%", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(28.dp)
                                            .height((pct * 0.9f).dp)
                                            .background(SelforaPrimary, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(date, fontSize = 9.sp, color = SelforaTextSecondary)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // B. Activity-Wise Progress (4 ADLs)
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("ACTIVITY-WISE INDEPENDENCE BREAKDOWN", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(12.dp))

                        ProgressCategoryBar("👕 Dressing (T-Shirt & Pants)", selectedChild.dressingPercentage / 100f, SelforaPrimary, "${selectedChild.dressingPercentage}%")
                        Spacer(modifier = Modifier.height(10.dp))
                        ProgressCategoryBar("🥄 Eating with Spoon", selectedChild.eatingPercentage / 100f, SelforaSecondary, "${selectedChild.eatingPercentage}%")
                        Spacer(modifier = Modifier.height(10.dp))
                        ProgressCategoryBar("🧼 Grooming & Washing", 0.60f, SelforaWarning, "60%")
                        Spacer(modifier = Modifier.height(10.dp))
                        ProgressCategoryBar("👟 Shoes & Socks", selectedChild.shoesPercentage / 100f, SelforaSuccess, "${selectedChild.shoesPercentage}%")
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // C. Assistance Distribution Chart
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("ASSISTANCE LEVEL DISTRIBUTION", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Text("Distribution across all assessed task steps", fontSize = 11.sp, color = SelforaTextSecondary)
                        Spacer(modifier = Modifier.height(12.dp))

                        ProgressCategoryBar("Level 0 – Independent", 0.40f, PromptLevel0Independent, "40%")
                        Spacer(modifier = Modifier.height(8.dp))
                        ProgressCategoryBar("Level 1 – Visual Prompt", 0.30f, PromptLevel1Visual, "30%")
                        Spacer(modifier = Modifier.height(8.dp))
                        ProgressCategoryBar("Level 2/3 – Gesture/Verbal", 0.20f, PromptLevel3Verbal, "20%")
                        Spacer(modifier = Modifier.height(8.dp))
                        ProgressCategoryBar("Level 5/6 – Physical Assistance", 0.10f, PromptLevel5PartialPhysical, "10%")
                    }
                }

                // TAB 2: SESSION TIMELINE
                "Session Timeline" -> {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text("LONGITUDINAL CLINICAL SESSION TIMELINE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Text("Assessment dates, therapy sessions, adaptations, and prompt fading events", fontSize = 11.sp, color = SelforaTextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        listOf(
                            TimelineEvent("Oct 02, 2026", "Therapy Session #6", "Dressing ADL", "78% Independence", "Prompt faded to Visual on Step 7", SelforaSuccess),
                            TimelineEvent("Oct 01, 2026", "Approved Adaptation Plan", "Dressing ADL", "Adaptation Active", "Therapist approved visual prompt plan for right sleeve", SelforaSecondary),
                            TimelineEvent("Sep 29, 2026", "Therapy Session #5", "Spoon Feeding", "70% Independence", "Verbal prompt recorded on scoop step", SelforaPrimary),
                            TimelineEvent("Sep 25, 2026", "Therapy Session #4", "Velcro Shoes", "85% Independence", "Independent closure verified", SelforaSuccess),
                            TimelineEvent("Sep 15, 2026", "Clinical Baseline Assessment", "All ADLs", "Evaluated Baseline", "Initial clinical baseline evaluation completed", SelforaWarning)
                        ).forEach { item ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = item.color.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, item.color.copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                                        Text(item.date, fontSize = 10.sp, color = SelforaTextMuted)
                                    }
                                    Text("Activity: ${item.activity} • Score: ${item.score}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = item.color)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.details, fontSize = 11.sp, color = SelforaTextSecondary)
                                }
                            }
                        }
                    }
                }

                // TAB 3: CAREGIVER HOME PRACTICE TREND
                "Caregiver Practice" -> {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("CAREGIVER HOME PRACTICE TREND", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            Surface(shape = RoundedCornerShape(8.dp), color = SelforaSecondary.copy(alpha = 0.15f)) {
                                Text("Caregiver Logged", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Text("Log of practices submitted at home by ${selectedChild.caregiverName}", fontSize = 11.sp, color = SelforaTextSecondary)
                        Spacer(modifier = Modifier.height(14.dp))

                        // Explicit Data Separation Safeguard Note
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SelforaPurpleAccent.copy(alpha = 0.1f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SelforaSecondary.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("🔒 CLINICAL DATA SEPARATION SAFEGUARD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaSecondary)
                                Text("Caregiver-reported home practice outcomes are displayed separately and are NEVER merged into the therapist's official clinical assessment score.", fontSize = 10.sp, color = SelforaTextPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Total Practices Logged", fontSize = 11.sp, color = SelforaTextSecondary)
                                Text("12 Sessions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                            }
                            Column {
                                Text("Weekly Compliance", fontSize = 11.sp, color = SelforaTextSecondary)
                                Text("85% (4x/wk)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaSuccess)
                            }
                            Column {
                                Text("Completed Activities", fontSize = 11.sp, color = SelforaTextSecondary)
                                Text("4 ADLs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Caregiver Weekly Participation Grid (Week 38):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            DayCheckCard("Mon", true)
                            DayCheckCard("Tue", true)
                            DayCheckCard("Wed", false)
                            DayCheckCard("Thu", true)
                            DayCheckCard("Fri", true)
                            DayCheckCard("Sat", false)
                            DayCheckCard("Sun", true)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Caregiver Outcome Notes:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        Text("\"Child practiced T-shirt dressing with visual prompts on Mon, Thu, Fri. Performed head insertion independently!\"", fontSize = 12.sp, color = SelforaTextSecondary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}

@Composable
fun ProgressCategoryBar(label: String, percentage: Float, color: Color, percentageText: String) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
            Text(percentageText, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
        )
    }
}

@Composable
fun DayCheckCard(day: String, isCompleted: Boolean) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isCompleted) SelforaSuccess.copy(alpha = 0.15f) else SelforaBorder.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isCompleted) SelforaSuccess else SelforaBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(day, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(if (isCompleted) "✓" else "—", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = if (isCompleted) SelforaSuccess else SelforaTextMuted)
        }
    }
}

private data class TimelineEvent(
    val date: String,
    val title: String,
    val activity: String,
    val score: String,
    val details: String,
    val color: Color
)
