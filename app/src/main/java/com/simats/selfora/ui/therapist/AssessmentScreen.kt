package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.*
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentScreen(
    childId: Long,
    onAssessmentSaved: (assessmentId: Long) -> Unit,
    onBack: () -> Unit
) {
    var selectedActivityId by remember { mutableStateOf(1L) } // 1L = T-Shirt
    var steps by remember { mutableStateOf<List<TaskStepResponse>>(emptyList()) }
    var promptRatings by remember { mutableStateOf<MutableMap<Long, Int>>(mutableMapOf()) }
    var outcomeRatings by remember { mutableStateOf<MutableMap<Long, String>>(mutableMapOf()) }
    var notes by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(selectedActivityId) {
        isLoading = true
        try {
            val res = ApiClient.apiService.getTaskSteps(selectedActivityId)
            if (res.isSuccessful && res.body() != null) {
                steps = res.body()!!
            } else {
                steps = getSampleTShirtSteps()
            }
        } catch (e: Exception) {
            steps = getSampleTShirtSteps()
        } finally {
            val initialPrompts = mutableMapOf<Long, Int>()
            val initialOutcomes = mutableMapOf<Long, String>()
            steps.forEach { step ->
                initialPrompts[step.id] = 3 // default Verbal Prompt
                initialOutcomes[step.id] = "SUCCESS"
            }
            promptRatings = initialPrompts
            outcomeRatings = initialOutcomes
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ADL Baseline Assessment", fontWeight = FontWeight.Bold, color = SelforaTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SelforaSurface,
                    titleContentColor = SelforaTextPrimary,
                    navigationIconContentColor = SelforaTextPrimary,
                    actionIconContentColor = SelforaTextPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBgLight)
                .padding(16.dp)
        ) {
            Text("Select Activity:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = selectedActivityId == 1L, onClick = { selectedActivityId = 1L }, label = { Text("T-Shirt") })
                FilterChip(selected = selectedActivityId == 3L, onClick = { selectedActivityId = 3L }, label = { Text("Jacket") })
                FilterChip(selected = selectedActivityId == 8L, onClick = { selectedActivityId = 8L }, label = { Text("Socks") })
                FilterChip(selected = selectedActivityId == 13L, onClick = { selectedActivityId = 13L }, label = { Text("Eating (Spoon)") })
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(steps) { index, step ->
                        val currentPrompt = promptRatings[step.id] ?: 3
                        val currentOutcome = outcomeRatings[step.id] ?: "SUCCESS"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Step ${step.stepNumber}: ${step.title}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SelforaBlueDark
                                )
                                Text(step.instructionText, fontSize = 12.sp, color = SelforaTextMuted)

                                Spacer(modifier = Modifier.height(12.dp))

                                Text("Required Baseline Prompt Level:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(6.dp))

                                PromptHierarchyBar(
                                    selectedLevel = currentPrompt,
                                    isUnable = currentOutcome == "UNABLE",
                                    onLevelSelected = { lvl ->
                                        promptRatings[step.id] = lvl
                                        outcomeRatings[step.id] = "SUCCESS"
                                        promptRatings = HashMap(promptRatings)
                                        outcomeRatings = HashMap(outcomeRatings)
                                    },
                                    onUnableSelected = {
                                        outcomeRatings[step.id] = "UNABLE"
                                        outcomeRatings = HashMap(outcomeRatings)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isSaving = true
                            try {
                                val stepReqs = steps.map { step ->
                                    StepAssessmentRequest(
                                        stepId = step.id,
                                        promptLevelId = promptRatings[step.id] ?: 3,
                                        outcome = outcomeRatings[step.id] ?: "SUCCESS"
                                    )
                                }
                                val req = CreateAssessmentRequest(
                                    childId = childId,
                                    activityId = selectedActivityId,
                                    notes = notes,
                                    stepResults = stepReqs
                                )
                                val res = ApiClient.apiService.createAssessment(req)
                                if (res.isSuccessful && res.body() != null) {
                                    onAssessmentSaved(res.body()!!.assessmentId)
                                } else {
                                    onAssessmentSaved(1L)
                                }
                            } catch (e: Exception) {
                                onAssessmentSaved(1L)
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaBluePrimary)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SAVE BASELINE ASSESSMENT", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PromptHierarchyBar(
    selectedLevel: Int,
    isUnable: Boolean,
    onLevelSelected: (Int) -> Unit,
    onUnableSelected: () -> Unit
) {
    val row1 = listOf(
        0 to "L0 Indep",
        1 to "L1 Visual",
        2 to "L2 Gesture",
        3 to "L3 Verbal"
    )
    val row2 = listOf(
        4 to "L4 Model",
        5 to "L5 Partial",
        6 to "L6 Full"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            row1.forEach { (lvl, label) ->
                val isSelected = !isUnable && selectedLevel == lvl
                val color = getPromptHierarchyColor(lvl.toString())
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clickable { onLevelSelected(lvl) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) color else color.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else color.copy(alpha = 0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else SelforaTextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            row2.forEach { (lvl, label) ->
                val isSelected = !isUnable && selectedLevel == lvl
                val color = getPromptHierarchyColor(lvl.toString())
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .clickable { onLevelSelected(lvl) },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) color else color.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else color.copy(alpha = 0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else SelforaTextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }

            // UNABLE button in row 2
            val isUnableSelected = isUnable
            val unableColor = PromptLevelUnable
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .clickable { onUnableSelected() },
                shape = RoundedCornerShape(10.dp),
                color = if (isUnableSelected) unableColor else unableColor.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (isUnableSelected) unableColor else unableColor.copy(alpha = 0.4f))
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "UNABLE",
                        fontSize = 11.sp,
                        fontWeight = if (isUnableSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isUnableSelected) Color.White else SelforaTextPrimary,
                        maxLines = 1
                    )
                }
            }
        }
    }
}


fun getSampleTShirtSteps(): List<TaskStepResponse> {
    val titles = listOf(
        "Look at the T-shirt", "Pick up the T-shirt", "Identify front and back", "Find neck opening",
        "Hold T-shirt at shoulder areas", "Lift T-shirt to chest level", "Put head through neck opening",
        "Pull T-shirt down", "Find right sleeve", "Put right arm through sleeve", "Pull right sleeve toward shoulder",
        "Find left sleeve", "Put left arm through sleeve", "Pull left sleeve toward shoulder",
        "Pull front down", "Pull back down", "Straighten T-shirt", "Check comfortable positioning"
    )
    val childTexts = listOf(
        "Look at your cool T-Shirt in the cupboard! 👦", "Pick up your T-shirt with both hands! 👕", "Find the tag on the back! 🏷️",
        "Look for the big neck hole! ⭕", "Hold your shirt at the shoulders!", "Lift the shirt up high to your chest! 🌟",
        "Pop your head through the neck hole! Peekaboo! 👦✨", "Pull the shirt down past your head!",
        "Find the right arm sleeve! 🖐️", "Push your right arm through! 💪", "Pull the right sleeve up!",
        "Find the left arm sleeve! 🖐️", "Push your left arm through! 💪", "Pull the left sleeve up!",
        "Pull the front down to your tummy!", "Pull the back down over your waist!", "Smooth out your T-shirt!",
        "You look awesome! All set! 🎉"
    )
    return titles.mapIndexed { index, title ->
        TaskStepResponse(
            id = (index + 1).toLong(),
            activityId = 1L,
            stepNumber = index + 1,
            title = title,
            instructionText = "Step ${index + 1}: $title",
            childInstruction = childTexts[index],
            audioPromptUrl = null
        )
    }
}

fun getSampleGirlFrockSteps(): List<TaskStepResponse> {
    val titles = listOf(
        "Look at the Frock", "Pick up the Frock", "Identify front and back tag", "Find neck opening",
        "Hold Frock at shoulder straps", "Lift Frock to chest level", "Put head through neck opening",
        "Pull Frock down past head", "Find right armhole", "Put right arm through armhole", "Pull right sleeve to shoulder",
        "Find left armhole", "Put left arm through armhole", "Pull left sleeve to shoulder",
        "Pull dress front down", "Pull skirt back down", "Straighten & adjust frock", "Check comfortable fit"
    )
    val childTexts = listOf(
        "Look at your beautiful Frock in the cupboard! 👧✨", "Pick up your Frock with both hands! 👗", "Find the tag on the back! 🏷️",
        "Look for the top neck hole! ⭕", "Hold your frock at the shoulder straps!", "Lift the frock up high to your chest! 🌟",
        "Pop your head through the neck hole! Peekaboo! 👧✨", "Pull the frock down past your head!",
        "Find the right arm hole! 🖐️", "Push your right arm through! 💪", "Pull the right sleeve up!",
        "Find the left arm hole! 🖐️", "Push your left arm through! 💪", "Pull the left sleeve up!",
        "Pull the dress front down to your waist!", "Pull the back skirt down smooth!", "Smooth out your pretty frock!",
        "You look like a princess! All ready! 💖👑"
    )
    return titles.mapIndexed { index, title ->
        TaskStepResponse(
            id = (100 + index + 1).toLong(),
            activityId = 2L,
            stepNumber = index + 1,
            title = title,
            instructionText = "Step ${index + 1}: $title",
            childInstruction = childTexts[index],
            audioPromptUrl = null
        )
    }
}

fun getSampleEatingSteps(): List<TaskStepResponse> {
    val titles = listOf(
        "Look at food & spoon", "Hold spoon with proper grip", "Dip spoon into bowl",
        "Scoop food into spoon", "Lift spoon to mouth", "Open mouth wide",
        "Place food into mouth", "Chew food thoroughly", "Swallow food comfortably", "Wipe mouth with napkin"
    )
    val childTexts = listOf(
        "Look at your tasty meal and spoon! 🥣🥄", "Hold your spoon firmly! 🥄", "Dip your spoon into the food! 🍲",
        "Scoop up a yummy mouthful! 😋", "Lift the spoon carefully to your mouth! 🌟", "Open your mouth wide! Ahhh! 😮",
        "Put the spoon inside your mouth! Yum! 😋", "Chew your food nicely! 🍎", "Swallow your bite! Good job! 👍",
        "Wipe your mouth with a napkin! Clean & happy! ✨"
    )
    return titles.mapIndexed { index, title ->
        TaskStepResponse(
            id = (200 + index + 1).toLong(),
            activityId = 13L,
            stepNumber = index + 1,
            title = title,
            instructionText = "Step ${index + 1}: $title",
            childInstruction = childTexts[index],
            audioPromptUrl = null
        )
    }
}

fun getSampleShoesSteps(): List<TaskStepResponse> {
    val titles = listOf(
        "Look at shoes & socks", "Pick up right sock", "Pull sock open with hands",
        "Push toes into sock", "Pull sock over heel", "Unfasten velcro straps",
        "Push foot into shoe", "Pull heel tab up", "Press velcro strap tight", "Stand up & check fit"
    )
    val childTexts = listOf(
        "Look at your cool shoes and socks! 👟", "Pick up your right sock! 🧦", "Open up the sock hole with both hands! ⭕",
        "Slide your toes into the sock! 🦶", "Pull the sock up over your heel! 🧦", "Open up the shoe velcro straps! 👟",
        "Slide your foot all the way into the shoe! 🦶", "Pull up the shoe back tab! 👟", "Press the velcro strap tight and secure! 🔒",
        "You put on your shoes! Stand up & high five! 🙌⭐"
    )
    return titles.mapIndexed { index, title ->
        TaskStepResponse(
            id = (300 + index + 1).toLong(),
            activityId = 9L,
            stepNumber = index + 1,
            title = title,
            instructionText = "Step ${index + 1}: $title",
            childInstruction = childTexts[index],
            audioPromptUrl = null
        )
    }
}


