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
import com.simats.selfora.data.model.ChildSummaryItem
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistProgressScreen(
    childId: Long = 1L,
    onBack: () -> Unit
) {
    // List of children available for progress selection
    val availableChildren = remember {
        listOf(
            ChildSummaryItem(1L, "Arjun Kumar", 8, "Priya Kumar", "Mother", 72, 65, 80, true, true),
            ChildSummaryItem(2L, "Ananya Reddy", 6, "Suresh Reddy", "Father", 85, 90, 78, false, true),
            ChildSummaryItem(3L, "Kavya Patel", 7, "Meera Patel", "Mother", 45, 50, 60, true, false),
            ChildSummaryItem(4L, "Rohan Sharma", 9, "Sunita Sharma", "Mother", 92, 88, 95, false, true)
        )
    }

    var selectedChild by remember {
        mutableStateOf(availableChildren.find { it.id == childId } ?: availableChildren.first())
    }
    var selectedTab by remember { mutableStateOf("Prompt Fading") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Therapist Progress & Analytics", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SelforaTextPrimary)
                        Text("Selected: ${selectedChild.name}", fontSize = 12.sp, color = SelforaPrimary, fontWeight = FontWeight.SemiBold)
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
                .padding(16.dp)
        ) {
            // STEP 1: SELECT CHILD NAME FIRST
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("SELECT CHILD TO VIEW PROGRESS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Child Selection Chips
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
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (child.name.contains("Ananya") || child.name.contains("Kavya")) "👧" else "👦", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = child.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else SelforaTextPrimary
                                    )
                                    Text(
                                        text = "Caregiver: ${child.caregiverName}",
                                        fontSize = 10.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else SelforaTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // STEP 2: DISPLAY DETAILS FOR SELECTED CHILD ONLY
            Text(
                text = "PROGRESS DETAILS — ${selectedChild.name.uppercase()}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Overall Independence Breakdown Card
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(selectedChild.name, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        Text("Caregiver: ${selectedChild.caregiverName} (${selectedChild.caregiverRelationship})", fontSize = 12.sp, color = SelforaTextSecondary)
                    }
                    Surface(shape = CircleShape, color = SelforaBlueLight, modifier = Modifier.size(40.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(if (selectedChild.name.contains("Ananya") || selectedChild.name.contains("Kavya")) "👧" else "👦", fontSize = 20.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SelforaBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                Text("INDEPENDENCE BREAKDOWN (DRESSING)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                val indepPct = selectedChild.dressingPercentage / 100f
                val promptPct = (100 - selectedChild.dressingPercentage - 10) / 100f

                ProgressCategoryBar("Independent (L0)", indepPct, PromptLevel0Independent, "${selectedChild.dressingPercentage}%")
                Spacer(modifier = Modifier.height(10.dp))
                ProgressCategoryBar("Prompt Required (L1-L6)", promptPct.coerceAtLeast(0.1f), PromptLevel2Gesture, "${(promptPct * 100).toInt()}%")
                Spacer(modifier = Modifier.height(10.dp))
                ProgressCategoryBar("Unable / Refused", 0.10f, PromptLevelUnable, "10%")
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Sub-tabs: Prompt Fading Timeline vs Caregiver Practice Review
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GlassChip(
                    text = "Prompt Fading Timeline",
                    selected = selectedTab == "Prompt Fading",
                    onClick = { selectedTab = "Prompt Fading" },
                    modifier = Modifier.weight(1f)
                )
                GlassChip(
                    text = "Caregiver Home Practice",
                    selected = selectedTab == "Home Practice",
                    onClick = { selectedTab = "Home Practice" },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == "Prompt Fading") {
                // Section 13 Prompt Fading Timeline across multiple sessions
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("STEP-LEVEL PROMPT FADING TIMELINE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    Text("Historical prompt reduction for ${selectedChild.name} over recent clinical sessions", fontSize = 12.sp, color = SelforaTextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))

                    PromptFadingStepItem(
                        stepNumber = 7,
                        stepTitle = "Put head through neck opening",
                        history = listOf(
                            FadingSessionRecord("Session 1 (Sep 10)", "Full Physical", PromptLevel6FullPhysical, "Needed full hand-over-hand guidance"),
                            FadingSessionRecord("Session 2 (Sep 15)", "Verbal", PromptLevel3Verbal, "Responded well to spoken cue"),
                            FadingSessionRecord("Session 3 (Sep 20)", "Verbal", PromptLevel3Verbal, "Needed slight reminder"),
                            FadingSessionRecord("Session 4 (Sep 25)", "Visual", PromptLevel1Visual, "Gazed at animation guide and initiated"),
                            FadingSessionRecord("Session 5 (Sep 29)", "Independent", PromptLevel0Independent, "Completed independently without prompt")
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = SelforaBorder.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    PromptFadingStepItem(
                        stepNumber = 10,
                        stepTitle = "Push right arm through sleeve",
                        history = listOf(
                            FadingSessionRecord("Session 1 (Sep 10)", "Partial Physical", PromptLevel5PartialPhysical, "Guided right elbow"),
                            FadingSessionRecord("Session 3 (Sep 20)", "Gesture", PromptLevel2Gesture, "Pointed at sleeve hole"),
                            FadingSessionRecord("Session 5 (Sep 29)", "Visual", PromptLevel1Visual, "Prompted by mirror view")
                        )
                    )
                }
            } else {
                // Section 16 Caregiver Home Practice Review
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text("CAREGIVER HOME PRACTICE REVIEW", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    Text("Review practice logged at home by ${selectedChild.caregiverName} (${selectedChild.caregiverRelationship})", fontSize = 12.sp, color = SelforaTextSecondary)
                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Activity: T-Shirt Dressing • Week 38", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Days Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        DayCheckCard("Mon", true)
                        DayCheckCard("Tue", true)
                        DayCheckCard("Wed", false)
                        DayCheckCard("Thu", true)
                        DayCheckCard("Fri", true)
                        DayCheckCard("Sat", false)
                        DayCheckCard("Sun", true)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Caregiver Observation Box
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SelforaPurpleAccent.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SelforaSecondary.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FormatQuote, contentDescription = null, tint = SelforaSecondary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Caregiver Observation Note", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaSecondary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "\"${selectedChild.name} attempted step 7 independently on Monday! Needed a little help with the right sleeve on Thursday.\"",
                                fontSize = 13.sp,
                                color = SelforaTextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
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

data class FadingSessionRecord(
    val sessionName: String,
    val promptName: String,
    val promptColor: Color,
    val note: String
)

@Composable
fun PromptFadingStepItem(
    stepNumber: Int,
    stepTitle: String,
    history: List<FadingSessionRecord>
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = SelforaPrimary.copy(alpha = 0.15f), modifier = Modifier.size(28.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("$stepNumber", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("Step $stepNumber: $stepTitle", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Vertical Timeline
        Column(modifier = Modifier.padding(start = 14.dp)) {
            history.forEachIndexed { index, rec ->
                Row(verticalAlignment = Alignment.Top) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(shape = CircleShape, color = rec.promptColor, modifier = Modifier.size(12.dp)) {}
                        if (index < history.size - 1) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(34.dp)
                                    .background(SelforaBorder)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.padding(bottom = 8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(rec.sessionName, fontSize = 11.sp, color = SelforaTextSecondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(shape = RoundedCornerShape(8.dp), color = rec.promptColor.copy(alpha = 0.15f)) {
                                Text(rec.promptName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = rec.promptColor, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        Text(rec.note, fontSize = 12.sp, color = SelforaTextPrimary)
                    }
                }
            }
        }
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
