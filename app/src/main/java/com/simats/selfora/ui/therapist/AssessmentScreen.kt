package com.simats.selfora.ui.therapist

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.components.VisualStepGuidanceCard
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*

data class TaskStepItem(
    val stepNumber: Int,
    val title: String,
    val instruction: String,
    val mediaUrl: String? = null
)

val BOY_TSHIRT_STEPS = listOf(
    TaskStepItem(1, "Pick up T-shirt", "Pick up shirt holding bottom hem with both hands"),
    TaskStepItem(2, "Orient front side", "Ensure front graphic is facing away from body"),
    TaskStepItem(3, "Gather bottom hem", "Gather bottom edge in hands preparing for neck opening"),
    TaskStepItem(4, "Find neck hole", "Locate and open neck hole wide"),
    TaskStepItem(5, "Lift over head", "Lift shirt up toward face"),
    TaskStepItem(6, "Position over head", "Place neck opening directly over head"),
    TaskStepItem(7, "Put head through neck opening", "Push head up and through the neck collar opening"),
    TaskStepItem(8, "Lower shirt past neck", "Pull neck band down onto shoulders"),
    TaskStepItem(9, "Find right armhole", "Locate right sleeve opening"),
    TaskStepItem(10, "Push right arm through sleeve", "Insert right hand and extend arm fully through right sleeve"),
    TaskStepItem(11, "Find left armhole", "Locate left sleeve opening"),
    TaskStepItem(12, "Push left arm through sleeve", "Insert left hand and extend arm fully through left sleeve"),
    TaskStepItem(13, "Pull shirt torso down", "Grasp front hem and pull down over chest"),
    TaskStepItem(14, "Pull back hem down", "Reach back and pull shirt down over waist"),
    TaskStepItem(15, "Adjust shoulders", "Smooth out shoulder seams"),
    TaskStepItem(16, "Unfold twisted sleeves", "Adjust sleeve cuffs if rolled or stuck"),
    TaskStepItem(17, "Straighten bottom edge", "Check bottom edge is flat around waist"),
    TaskStepItem(18, "Final check", "Stand tall and inspect shirt fit independently")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentScreen(
    childId: Long,
    onAssessmentSaved: () -> Unit,
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("DRESSING") }
    var selectedActivity by remember { mutableStateOf("Boy T-Shirt Dressing") }
    var currentStepIndex by remember { mutableStateOf(6) } // Step 7 (0-indexed 6)
    var selectedPromptLevel by remember { mutableStateOf("L3 Verbal") }
    var observationText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val totalSteps = BOY_TSHIRT_STEPS.size
    val currentStep = BOY_TSHIRT_STEPS[currentStepIndex]

    val promptLevels = listOf(
        "L0 Independent",
        "L1 Visual",
        "L2 Gesture",
        "L3 Verbal",
        "L4 Model / Video",
        "L5 Partial Physical",
        "L6 Full Physical",
        "Unable"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Therapist Clinical Assessment", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SelforaTextPrimary)
                        Text("Child ID: #$childId • $selectedActivity", fontSize = 12.sp, color = SelforaTextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaTextPrimary)
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
            // ADL Activity Selection Bar
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("SELECT ADL TASK CATEGORY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassChip(text = "👕 Dressing (18 steps)", selected = selectedCategory == "DRESSING", onClick = { selectedCategory = "DRESSING"; selectedActivity = "Boy T-Shirt Dressing" }, modifier = Modifier.weight(1f))
                    GlassChip(text = "🥄 Eating (10 steps)", selected = selectedCategory == "EATING", onClick = { selectedCategory = "EATING"; selectedActivity = "Spoon Eating" }, modifier = Modifier.weight(1f))
                    GlassChip(text = "👟 Shoes (10 steps)", selected = selectedCategory == "SHOES", onClick = { selectedCategory = "SHOES"; selectedActivity = "Shoes & Socks" }, modifier = Modifier.weight(1f))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step Progress Header
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$selectedActivity",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = SelforaTextPrimary
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SelforaPrimary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Step ${currentStep.stepNumber} of $totalSteps",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = (currentStepIndex + 1) / totalSteps.toFloat(),
                    color = SelforaPrimary,
                    trackColor = SelforaPrimary.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animation / Visual Step Guidance Card
            VisualStepGuidanceCard(
                stepNumber = currentStep.stepNumber,
                totalSteps = totalSteps,
                stepTitle = currentStep.title,
                childInstruction = currentStep.instruction,
                gender = "BOY",
                onSpeakInstruction = {}
            )

            Spacer(modifier = Modifier.height(18.dp))

            // THERAPIST PROMPT LEVEL RECORDING MODULE (ONLY IN THERAPIST MODE)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.95f),
                borderColor = SelforaPrimary.copy(alpha = 0.4f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Psychology, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Therapist Prompt Level (Clinical Only)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Prompt Level Grid Buttons
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    promptLevels.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            pair.forEach { level ->
                                val isSelected = selectedPromptLevel == level
                                val levelColor = getPromptHierarchyColor(level)

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .shadow(if (isSelected) 6.dp else 1.dp, shape = RoundedCornerShape(16.dp))
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) levelColor else Color.White)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) Color.White else levelColor.copy(alpha = 0.4f),
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                        .clickable { selectedPromptLevel = level }
                                        .padding(vertical = 12.dp, horizontal = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = level,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else SelforaTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Observation Input
                GlassTextField(
                    value = observationText,
                    onValueChange = { observationText = it },
                    label = "Clinical Observation & Fading Notes",
                    placeholder = "Add clinical notes on child's response or assistance required...",
                    singleLine = false,
                    leadingIcon = Icons.Default.EditNote
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (currentStepIndex > 0) {
                        GlassOutlinedButton(
                            text = "← Previous",
                            onClick = { currentStepIndex-- },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    GlassButton(
                        text = if (currentStepIndex == totalSteps - 1) "Finish Assessment ✓" else "Save & Next Step →",
                        onClick = {
                            Toast.makeText(context, "Step ${currentStep.stepNumber} recorded as $selectedPromptLevel", Toast.LENGTH_SHORT).show()
                            if (currentStepIndex < totalSteps - 1) {
                                currentStepIndex++
                                observationText = ""
                            } else {
                                onAssessmentSaved()
                            }
                        },
                        modifier = Modifier.weight(1.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}
