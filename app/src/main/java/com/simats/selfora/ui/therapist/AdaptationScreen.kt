package com.simats.selfora.ui.therapist

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.ClinicalPromptLevel
import com.simats.selfora.data.model.CreateAdaptivePlanRequest
import com.simats.selfora.data.model.PerformanceRecordResponse
import com.simats.selfora.data.model.SessionResponse
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptationScreen(
    sessionId: Long,
    childId: Long,
    activityId: Long,
    onAdaptationSaved: () -> Unit,
    onBack: () -> Unit
) {
    var session by remember { mutableStateOf<SessionResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadTrigger by remember { mutableStateOf(0) }

    var analysisText by remember { mutableStateOf("") }
    var adaptPlanText by remember { mutableStateOf("") }
    var selectedCurrentLevel by remember { mutableStateOf<ClinicalPromptLevel?>(null) }
    var selectedTargetLevel by remember { mutableStateOf<ClinicalPromptLevel?>(null) }
    var selectedStepIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var isConfirmed by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }
    var isInitialized by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(sessionId, childId, activityId, reloadTrigger) {
        if (sessionId <= 0 || childId <= 0) {
            errorMessage = "Invalid session or child identifier."
            isLoading = false
            return@LaunchedEffect
        }
        isLoading = true
        errorMessage = null
        try {
            val res = ApiClient.apiService.getSessionById(sessionId)
            if (res.isSuccessful && res.body() != null) {
                val s = res.body()!!
                session = s

                // Initialize fields if first load
                if (!isInitialized) {
                    val records = s.performanceRecords
                    val promptedSteps = records.filter {
                        it.promptLevelId > 0 || it.outcome.equals("UNABLE", ignoreCase = true)
                    }

                    if (promptedSteps.isNotEmpty()) {
                        selectedStepIds = promptedSteps.map { it.stepId }.toSet()
                        val highestPrompt = promptedSteps.maxByOrNull { it.promptLevelId }
                        val curLvl = ClinicalPromptLevel.fromId(highestPrompt?.promptLevelId) ?: ClinicalPromptLevel.L3
                        selectedCurrentLevel = curLvl
                        val tgtId = (curLvl.id - 1).coerceAtLeast(0)
                        selectedTargetLevel = ClinicalPromptLevel.fromId(tgtId) ?: ClinicalPromptLevel.L0
                    } else if (records.isNotEmpty()) {
                        selectedStepIds = records.take(1).map { it.stepId }.toSet()
                        selectedCurrentLevel = ClinicalPromptLevel.L0
                        selectedTargetLevel = ClinicalPromptLevel.L0
                    }

                    // Pre-fill analysis with factual session observation
                    if (!s.summaryNotes.isNullOrBlank()) {
                        analysisText = s.summaryNotes
                    } else if (records.isNotEmpty()) {
                        val indepCount = s.independentSteps ?: records.count { it.promptLevelId == 0 && it.outcome.equals("COMPLETED", ignoreCase = true) }
                        val totalCount = s.totalSteps ?: records.size
                        val pct = s.independencePercentage?.toInt() ?: ((indepCount * 100) / totalCount)
                        analysisText = "${s.childName} performed ${s.activityTitle} with $pct% independence ($indepCount/$totalCount steps independent). Prompt levels used: ${s.mostFrequentPromptLevel ?: "Various"}."
                    }

                    // Pre-fill adaptation plan text
                    if (selectedCurrentLevel != null && selectedTargetLevel != null) {
                        if (selectedCurrentLevel == ClinicalPromptLevel.L0) {
                            adaptPlanText = "Maintain natural environmental cues and practice independently in home environment."
                        } else {
                            adaptPlanText = "Fade from ${selectedCurrentLevel?.label} to ${selectedTargetLevel?.label} on targeted steps during next sessions."
                        }
                    }
                    isInitialized = true
                }
            } else {
                errorMessage = "Failed to load session details (HTTP ${res.code()})"
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.localizedMessage ?: "Failed to connect to backend"}"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Analyze & Adapt",
                subtitle = session?.let { "${it.childName} • ${it.activityTitle}" } ?: "Session #$sessionId",
                onBackClick = onBack,
                accentColor = SelforaPrimary
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = SelforaPrimary)
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Loading session adaptation data...",
                        fontSize = 13.5.sp,
                        color = SelforaTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = SelforaError, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Adaptation Data Error",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = errorMessage ?: "Unable to load session data.",
                            color = SelforaTextSecondary,
                            fontSize = 13.5.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = { reloadTrigger++ }, modifier = Modifier.weight(1f)) {
                                Text("Retry")
                            }
                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Go Back")
                            }
                        }
                    }
                }
            }
        } else if (session == null || session!!.performanceRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Insufficient Performance Data",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No recorded step evaluations were found for session #${sessionId}. Complete a therapy session with step evaluations before creating an adaptive plan.",
                            color = SelforaTextSecondary,
                            fontSize = 13.5.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Back to Session")
                        }
                    }
                }
            }
        } else {
            val s = session!!
            val records = s.performanceRecords

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Child & Session Context Card
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = s.childName,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaTextPrimary
                                )
                                Text(
                                    text = "${s.activityTitle} • Session #${s.sessionId}",
                                    fontSize = 13.sp,
                                    color = SelforaTextSecondary
                                )
                            }
                            val indepPct = s.independencePercentage ?: 0.0
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SelforaPrimary.copy(alpha = 0.12f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SelforaPrimary.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = "${Math.round(indepPct)}% Independence",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card 1: Clinical Analysis & Factual Observations
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clinical Analysis & Observations", fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Document actual child performance, prompts required, and behavioral observations",
                        fontSize = 11.5.sp,
                        color = SelforaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    GlassTextField(
                        value = analysisText,
                        onValueChange = { analysisText = it },
                        label = "",
                        placeholder = "Record clinical analysis for ${s.childName}...",
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card 2: Adaptive Strategy & Fading Plan
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingDown, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Adaptation Strategy & Fading Plan", fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Specify prompt fading trajectory and environmental adaptations",
                        fontSize = 11.5.sp,
                        color = SelforaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    GlassTextField(
                        value = adaptPlanText,
                        onValueChange = { adaptPlanText = it },
                        label = "",
                        placeholder = "Describe prompt fading strategy for targeted steps...",
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card 3: Next Step Focus (Dynamic Task Steps from Session)
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Next Step Focus", fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        }
                        Text(
                            text = "${selectedStepIds.size} selected",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Select steps to target for prompt reduction or reinforcement (tap step to toggle):",
                        fontSize = 11.5.sp,
                        color = SelforaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        records.sortedBy { it.stepNumber }.forEach { record ->
                            val isSelected = selectedStepIds.contains(record.stepId)
                            val clinicalLevel = ClinicalPromptLevel.fromId(record.promptLevelId)
                                ?: ClinicalPromptLevel.fromCode(record.promptLevelCode)
                            val badgeColor = clinicalLevel?.composeColor ?: SelforaPrimary

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) SelforaPrimary.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.85f),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) SelforaPrimary else SelforaBorder.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedStepIds = if (isSelected) {
                                            selectedStepIds - record.stepId
                                        } else {
                                            selectedStepIds + record.stepId
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                selectedStepIds = if (checked) {
                                                    selectedStepIds + record.stepId
                                                } else {
                                                    selectedStepIds - record.stepId
                                                }
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = SelforaPrimary),
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "Step ${record.stepNumber}: ${record.stepTitle}",
                                                fontSize = 13.5.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                color = SelforaTextPrimary
                                            )
                                            if (!record.therapistNote.isNullOrBlank()) {
                                                Text(
                                                    text = "Observed: ${record.therapistNote}",
                                                    fontSize = 11.sp,
                                                    color = SelforaTextSecondary
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = badgeColor.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            text = record.promptLevelName.ifBlank { record.promptLevelCode },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeColor,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card 4: Clinical Prompt Levels (Current & Target)
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Target Prompt Level for Fading",
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Choose the clinical target level the child will be guided toward",
                        fontSize = 11.5.sp,
                        color = SelforaTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ClinicalPromptLevel.hierarchyLevels.forEach { prompt ->
                            val isTarget = selectedTargetLevel == prompt
                            val pColor = prompt.composeColor

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isTarget) pColor.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isTarget) 1.5.dp else 1.dp,
                                    if (isTarget) pColor else SelforaBorder.copy(alpha = 0.25f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTargetLevel = prompt }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = isTarget,
                                            onClick = { selectedTargetLevel = prompt },
                                            colors = RadioButtonDefaults.colors(selectedColor = pColor),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = prompt.label,
                                            fontSize = 13.sp,
                                            fontWeight = if (isTarget) FontWeight.Bold else FontWeight.Medium,
                                            color = SelforaTextPrimary
                                        )
                                    }
                                    Text(
                                        text = prompt.shortLabel,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = pColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Therapist Approval Switch
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Therapist Approval",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextPrimary
                            )
                            Text(
                                text = "Confirm and activate this adaptive plan for clinical records & home program",
                                fontSize = 11.5.sp,
                                color = SelforaTextSecondary
                            )
                        }
                        Switch(
                            checked = isConfirmed,
                            onCheckedChange = { isConfirmed = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = SelforaPrimary, checkedTrackColor = SelforaPrimary.copy(alpha = 0.35f))
                        )
                    }
                }

                if (saveError != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SelforaError.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SelforaError.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = saveError!!,
                            color = SelforaError,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Adaptation Button
                GlassButton(
                    text = if (isSaving) "Saving Adaptive Plan..." else "Save Adaptation",
                    onClick = {
                        if (isSaving) return@GlassButton
                        saveError = null

                        val targetSteps = if (selectedStepIds.isNotEmpty()) {
                            selectedStepIds.toList()
                        } else {
                            records.map { it.stepId }
                        }

                        if (targetSteps.isEmpty()) {
                            saveError = "Please select at least one task step for adaptation."
                            return@GlassButton
                        }

                        val curLvlId = selectedCurrentLevel?.id ?: 3
                        val tgtLvlId = selectedTargetLevel?.id ?: 1
                        val rationale = if (adaptPlanText.isNotBlank()) {
                            "${analysisText.trim()} | Plan: ${adaptPlanText.trim()}".trim()
                        } else {
                            analysisText.ifBlank { "Adaptive plan for session #${s.sessionId}" }
                        }

                        scope.launch {
                            isSaving = true
                            try {
                                val req = CreateAdaptivePlanRequest(
                                    childId = childId,
                                    activityId = activityId,
                                    sessionId = sessionId,
                                    currentPromptLevelId = curLvlId,
                                    targetPromptLevelId = tgtLvlId,
                                    targetStepIds = targetSteps,
                                    clinicalRationale = rationale,
                                    isConfirmed = isConfirmed
                                )
                                val res = ApiClient.apiService.createAdaptivePlan(req)
                                if (res.isSuccessful && res.body() != null) {
                                    Toast.makeText(context, "Adaptive plan saved successfully", Toast.LENGTH_SHORT).show()
                                    onAdaptationSaved()
                                } else {
                                    val errBody = res.errorBody()?.string()
                                    saveError = "Failed to save adaptive plan: HTTP ${res.code()}${if (!errBody.isNullOrBlank()) " ($errBody)" else ""}"
                                }
                            } catch (e: Exception) {
                                saveError = "Network error: ${e.localizedMessage ?: "Failed to save plan"}"
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    icon = if (isSaving) null else Icons.Default.Check,
                    gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Cancel", color = SelforaTextSecondary, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
