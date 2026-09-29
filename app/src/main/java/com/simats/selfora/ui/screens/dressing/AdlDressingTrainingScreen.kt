package com.simats.selfora.ui.screens.dressing

import android.os.Build.VERSION.SDK_INT
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.simats.selfora.data.model.dressing.DressingStep
import com.simats.selfora.data.model.dressing.GenderCategory
import com.simats.selfora.data.model.dressing.PromptLevel
import com.simats.selfora.ui.theme.*

@Composable
fun AdlDressingTrainingScreen(
    activityId: String = "boy_tshirt_activity",
    userRole: String = "ROLE_CHILD",
    onNavigateBack: () -> Unit = {},
    onViewProgress: () -> Unit = {},
    viewModel: DressingViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(activityId) {
        viewModel.loadActivity(activityId)
    }

    val activity = uiState.activity ?: return
    val currentStep = uiState.currentStep ?: return
    val stepIndex = uiState.currentStepIndex
    val totalSteps = uiState.totalSteps
    val isBoy = activity.gender == GenderCategory.BOY
    val isGirl = !isBoy || activity.activityId.contains("girl", ignoreCase = true) || activity.activityId.contains("frock", ignoreCase = true)

    // Exact Child Theme Colors matching ChildHomeScreen & ChildActivityStepScreen
    val outerBgColor = if (isGirl) ChildRewardsCardBg else ChildBlueCard
    val themeColor = if (isGirl) SelforaSecondary else SelforaPrimary
    val cardBg = if (isGirl) ChildRewardsCardBg else ChildDressingCardBg
    val headerTextColor = if (isGirl) SelforaSecondary else SelforaBlueDark
    val isTherapist = userRole.equals("ROLE_THERAPIST", ignoreCase = true) || userRole.equals("THERAPIST", ignoreCase = true)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(outerBgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(18.dp)
    ) {
        if (uiState.isCompleted) {
            DressingCompletionView(
                activityTitle = activity.title,
                isBoy = isBoy,
                themeColor = themeColor,
                onPracticeAgain = { viewModel.restartSession() },
                onViewProgress = onViewProgress,
                onBackToDressing = onNavigateBack
            )
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. TOP HEADER NAVIGATION ROW (Floating Pill anchored at very top)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = headerTextColor
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Text(
                            text = "Step ${stepIndex + 1} of $totalSteps",
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = headerTextColor
                        )
                    }

                    IconButton(onClick = { viewModel.toggleAutoSpeech() }) {
                        Icon(
                            imageVector = if (uiState.isAutoSpeechEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Auto Speech",
                            tint = if (uiState.isAutoSpeechEnabled) themeColor else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. MIDDLE SCROLLABLE CONTENT (Anchored right below header at TOP)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Main White Visual Step Guidance Card (Positioned right at the top!)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Category Badge & Mode Label Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = cardBg
                                ) {
                                    Text(
                                        text = if (isGirl) "Frock Dressing Step ${stepIndex + 1} of $totalSteps" else "T-Shirt Dressing Step ${stepIndex + 1} of $totalSteps",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = themeColor,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SelforaBackground
                                ) {
                                    Text(
                                        text = if (isGirl) "Girl Mode 👧" else "Boy Mode 👦",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SelforaTextSecondary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Visual Demonstration Animation Box
                            AnimationPlayerViewport(
                                step = currentStep,
                                isBoy = isBoy,
                                themeColor = themeColor,
                                cardBg = cardBg,
                                replayCount = uiState.currentReplayCount,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(210.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Step Clinical Title
                            Text(
                                text = currentStep.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Child Guidance Spoken Instruction Box with Round Voice Speaker Button
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = cardBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "SPOKEN INSTRUCTION:",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = themeColor
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "\"${currentStep.childGuidance}\"",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SelforaTextPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // Floating Round Speaker Button
                                    IconButton(
                                        onClick = { viewModel.speakCurrentGuidance() },
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(themeColor)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Speak Guidance",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Therapist Prompt Selector (Conditional Rendering for Therapist Role ONLY)
                    if (isTherapist) {
                        Spacer(modifier = Modifier.height(14.dp))
                        PromptLevelSelector(
                            selectedPrompt = uiState.currentPromptLevel,
                            themeColor = themeColor,
                            onPromptSelected = { viewModel.setPromptLevel(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. BOTTOM FIXED ACTION NAVIGATION BUTTONS (Symmetrically Aligned)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (stepIndex > 0) {
                        Button(
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = headerTextColor
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Previous",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.nextStep() },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChildGreenPlay),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
                    ) {
                        Text(
                            text = if (stepIndex == totalSteps - 1) "I DID IT! 🎉" else "NEXT STEP",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun AnimationPlayerViewport(
    step: DressingStep,
    isBoy: Boolean,
    themeColor: Color,
    cardBg: Color,
    replayCount: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg.copy(alpha = 0.5f))
            .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = step.assetPath,
            contentDescription = step.visualDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            error = painterResource(id = android.R.drawable.stat_notify_error),
            imageLoader = imageLoader
        )

        DynamicStepVisualFallback(
            stepNumber = step.stepNumber,
            visualHighlight = step.visualHighlight,
            isBoy = isBoy,
            themeColor = themeColor,
            cardBg = cardBg
        )

        if (replayCount > 0) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = themeColor.copy(alpha = 0.9f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Replayed x$replayCount",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DynamicStepVisualFallback(
    stepNumber: Int,
    visualHighlight: String,
    isBoy: Boolean,
    themeColor: Color,
    cardBg: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.scale(pulseScale)
        ) {
            Surface(
                shape = CircleShape,
                color = themeColor.copy(alpha = 0.15f),
                modifier = Modifier.size(100.dp)
            ) {}

            Text(
                text = if (isBoy) "👦" else "👧",
                fontSize = 54.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = themeColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = visualHighlight,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SelforaTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun PromptLevelSelector(
    selectedPrompt: PromptLevel,
    themeColor: Color,
    onPromptSelected: (PromptLevel) -> Unit
) {
    val promptLevels = remember {
        listOf(
            PromptLevel.LEVEL_0,
            PromptLevel.LEVEL_1,
            PromptLevel.LEVEL_2,
            PromptLevel.LEVEL_3,
            PromptLevel.LEVEL_4,
            PromptLevel.LEVEL_5,
            PromptLevel.LEVEL_6,
            PromptLevel.UNABLE
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Therapist Prompt Level",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Text(
                    text = selectedPrompt.displayName,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                promptLevels.take(4).forEach { prompt ->
                    val isSelected = selectedPrompt == prompt
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPromptSelected(prompt) },
                        label = {
                            Text(
                                text = "L${prompt.level}: ${prompt.displayName}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                promptLevels.drop(4).forEach { prompt ->
                    val isSelected = selectedPrompt == prompt
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPromptSelected(prompt) },
                        label = {
                            Text(
                                text = if (prompt == PromptLevel.UNABLE) "Unable" else "L${prompt.level}: ${prompt.displayName}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (prompt == PromptLevel.UNABLE) ErrorRed else themeColor,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DressingCompletionView(
    activityTitle: String,
    isBoy: Boolean,
    themeColor: Color,
    onPracticeAgain: () -> Unit,
    onViewProgress: () -> Unit,
    onBackToDressing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing)
        ),
        label = "starRotation"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = WarningAmber.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(160.dp)
                    .rotate(starRotation)
            )

            Text(
                text = if (isBoy) "👦✨" else "👧✨",
                fontSize = 72.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "🎉 Great Job!",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                color = themeColor
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "All clinical dressing steps completed for $activityTitle!",
            style = MaterialTheme.typography.titleMedium.copy(
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onPracticeAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = themeColor)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Practice Again",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onViewProgress,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SelforaSecondary)
        ) {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "View Progress",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onBackToDressing,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = themeColor),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = themeColor
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Back to Dressing",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
            )
        }
    }
}
