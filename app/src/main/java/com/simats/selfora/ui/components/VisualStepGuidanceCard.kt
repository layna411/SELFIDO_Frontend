package com.simats.selfora.ui.components

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.simats.selfora.ui.theme.*

/**
 * Real-time Step-Specific Visual Guidance Card for Boy 👦 & Girl 👧 OT Dressing Steps.
 * Supports animated GIFs (e.g. 20 Boy T-Shirt steps) with high-performance Coil decoding
 * and graceful vector fallbacks.
 */
@Composable
fun VisualStepGuidanceCard(
    stepNumber: Int,
    totalSteps: Int,
    stepTitle: String,
    childInstruction: String,
    gender: String = "BOY", // BOY or GIRL
    activityTitle: String = "",
    isTamil: Boolean = false,
    mediaUrl: String? = null,
    onSpeakInstruction: () -> Unit
) {
    val isGirlMode = gender.uppercase().contains("GIRL") || gender.uppercase().contains("FEMALE")
    val cardBg = if (isGirlMode) ChildRewardsCardBg else ChildDressingCardBg
    val themeColor = if (isGirlMode) SelforaSecondary else SelforaPrimary

    // Auto-resolve GIF for Boy T-Shirt steps 1..20 if mediaUrl is not explicitly supplied
    val resolvedMediaUrl = if (!mediaUrl.isNullOrBlank()) {
        mediaUrl
    } else if (!isGirlMode && stepNumber in 1..20 && (
            activityTitle.isBlank() ||
            activityTitle.contains("T-shirt", ignoreCase = true) ||
            activityTitle.contains("T-Shirt", ignoreCase = true) ||
            activityTitle.contains("டி-சர்ட்") ||
            activityTitle.contains("Dressing", ignoreCase = true)
        )) {
        "file:///android_asset/dressing/boy/tshirt/step_${String.format("%02d", stepNumber)}.gif"
    } else {
        null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp)),
        colors = CardDefaults.cardColors(containerColor = SelforaSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Step Category Header & Gender Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = cardBg
                ) {
                    val headerText = if (isTamil) {
                        if (activityTitle.isNotBlank()) "$activityTitle • படி $stepNumber / $totalSteps"
                        else "படி $stepNumber / $totalSteps"
                    } else {
                        if (activityTitle.isNotBlank()) "$activityTitle • Step $stepNumber of $totalSteps"
                        else "Step $stepNumber of $totalSteps"
                    }
                    Text(
                        text = headerText,
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
                    val genderLabel = if (isTamil) {
                        if (isGirlMode) "சிறுமி 👧" else "சிறுவன் 👦"
                    } else {
                        if (isGirlMode) "Girl Mode 👧" else "Boy Mode 👦"
                    }
                    Text(
                        text = genderLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SelforaTextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-time Step-Specific Visual Illustration Box (GIF or Vector Fallback)
            StepVisualIllustrationView(
                stepNumber = stepNumber,
                stepTitle = stepTitle,
                gender = gender,
                themeColor = themeColor,
                mediaUrl = resolvedMediaUrl
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Step Wording Title
            Text(
                text = stepTitle,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Child Spoken Guidance Container with Voice Speaker Button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = cardBg,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTamil) "வழிகாட்டுதல் உரை:" else "SPOKEN INSTRUCTION:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "\"$childInstruction\"",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onSpeakInstruction,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(themeColor)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Speak",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders step-matching visual guidance with animated GIF support (Boy Baby T-shirt 1 to 20)
 * and animated vector fallback with pulse effects.
 */
@Composable
fun StepVisualIllustrationView(
    stepNumber: Int,
    stepTitle: String,
    gender: String,
    themeColor: Color,
    mediaUrl: String? = null
) {
    val context = LocalContext.current
    val isGirl = gender.uppercase().contains("GIRL") || gender.uppercase().contains("FEMALE")
    val titleLower = stepTitle.lowercase()

    // Setup Coil animated GIF decoder
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    // Pulse Animation for fallback indicator
    val infiniteTransition = rememberInfiniteTransition(label = "step_anim")
    val scalePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Accurate 20-step mapping with vector icons and child-friendly action descriptors
    val (illustrationIcon, actionBadgeText, bgTint) = when {
        // Step 1: Stand or sit in front of T-shirt
        stepNumber == 1 || titleLower.contains("stand") || titleLower.contains("sit in front") -> Triple(
            Icons.Default.AccessibilityNew,
            if (isGirl) "Girl 👧 standing calmly in front of Frock" else "Boy 👦 standing calmly in front of T-Shirt 👕",
            if (isGirl) ChildRewardsCardBg else ChildDressingCardBg
        )
        // Step 2: Look at T-shirt
        stepNumber == 2 || titleLower.contains("look at") -> Triple(
            Icons.Default.Visibility,
            if (isGirl) "Examining the Frock carefully 👧✨" else "Examining the T-shirt carefully 👀👕",
            if (isGirl) ChildRewardsCardBg else ChildDressingCardBg
        )
        // Step 3: Pick up T-shirt
        stepNumber == 3 || titleLower.contains("pick up") -> Triple(
            Icons.Default.FrontHand,
            if (isGirl) "Picking up Frock with both hands 🤲" else "Picking up T-shirt with both hands 🤲👕",
            ChildEatingCardBg
        )
        // Step 4: Identify front and back
        stepNumber == 4 || (titleLower.contains("front") && titleLower.contains("back")) -> Triple(
            Icons.Default.Checkroom,
            "Finding back collar tag & front graphic 🏷️",
            ChildDressingCardBg
        )
        // Step 5: Find bottom edge
        stepNumber == 5 || titleLower.contains("bottom edge") || titleLower.contains("bottom opening") -> Triple(
            Icons.Default.South,
            "Locating bottom hem opening 🔽",
            ChildEatingCardBg
        )
        // Step 6: Find neck opening
        stepNumber == 6 || titleLower.contains("neck opening") || titleLower.contains("neck hole") -> Triple(
            Icons.Default.CropFree,
            "Finding the round neck opening ⭕",
            ChildEatingCardBg
        )
        // Step 7: Hold T-shirt at shoulders
        stepNumber == 7 || titleLower.contains("shoulder areas") || titleLower.contains("hold") -> Triple(
            Icons.Default.PanTool,
            "Holding shoulder seams firmly 👕💪",
            ChildDressingCardBg
        )
        // Step 8: Lift T-shirt to chest level
        stepNumber == 8 || titleLower.contains("chest level") || titleLower.contains("lift") -> Triple(
            Icons.Default.North,
            "Lifting T-shirt up to chest level ⬆️",
            ChildShoesCardBg
        )
        // Step 9: Put head through neck opening
        stepNumber == 9 || (titleLower.contains("head") && titleLower.contains("neck")) -> Triple(
            Icons.Default.Face,
            if (isGirl) "Ananya popping head through frock! 👧✨" else "Aarav popping head through neck hole! 👦✨",
            ChildShoesCardBg
        )
        // Step 10: Pull T-shirt down over head
        stepNumber == 10 || (titleLower.contains("pull") && titleLower.contains("over the head")) -> Triple(
            Icons.Default.ArrowDownward,
            "Pulling collar smoothly past head onto neck 🔽",
            ChildShoesCardBg
        )
        // Step 11: Find right sleeve opening
        stepNumber == 11 || (titleLower.contains("right") && titleLower.contains("find")) -> Triple(
            Icons.Default.Search,
            "Locating right sleeve opening 🔎",
            ChildDressingCardBg
        )
        // Step 12: Put right arm through right sleeve
        stepNumber == 12 || (titleLower.contains("right") && titleLower.contains("through")) -> Triple(
            Icons.Default.PanTool,
            "Pushing right arm through right sleeve 💪👕",
            ChildDressingCardBg
        )
        // Step 13: Pull right sleeve up to shoulder
        stepNumber == 13 || (titleLower.contains("right") && titleLower.contains("shoulder")) -> Triple(
            Icons.Default.ArrowUpward,
            "Pulling right sleeve securely onto shoulder 👕",
            ChildDressingCardBg
        )
        // Step 14: Find left sleeve opening
        stepNumber == 14 || (titleLower.contains("left") && titleLower.contains("find")) -> Triple(
            Icons.Default.Search,
            "Locating left sleeve opening 🔎",
            ChildDressingCardBg
        )
        // Step 15: Put left arm through left sleeve
        stepNumber == 15 || (titleLower.contains("left") && titleLower.contains("through")) -> Triple(
            Icons.Default.PanTool,
            "Pushing left arm through left sleeve 💪👕",
            ChildDressingCardBg
        )
        // Step 16: Pull left sleeve up to shoulder
        stepNumber == 16 || (titleLower.contains("left") && titleLower.contains("shoulder")) -> Triple(
            Icons.Default.ArrowUpward,
            "Pulling left sleeve securely onto shoulder 👕",
            ChildDressingCardBg
        )
        // Step 17: Pull front down
        stepNumber == 17 || (titleLower.contains("front") && titleLower.contains("down")) -> Triple(
            Icons.Default.ArrowDownward,
            "Pulling front of shirt down over tummy 🔽",
            ChildShoesCardBg
        )
        // Step 18: Pull back down
        stepNumber == 18 || (titleLower.contains("back") && titleLower.contains("down")) -> Triple(
            Icons.Default.ArrowDownward,
            "Pulling back of shirt down over waist 🔽",
            ChildShoesCardBg
        )
        // Step 19: Straighten T-shirt
        stepNumber == 19 || titleLower.contains("straighten") -> Triple(
            Icons.Default.AutoFixHigh,
            "Straightening fabric at shoulders & waist ✨",
            ChildEatingCardBg
        )
        // Step 20: Completion check
        stepNumber == 20 || titleLower.contains("comfortable") || titleLower.contains("correctly") || titleLower.contains("mirror") -> Triple(
            Icons.Default.EmojiEvents,
            if (isGirl) "Girl 👧 completely dressed up! Looking great! 🎉⭐" else "Boy 👦 completely dressed up! Looking awesome! 🎉⭐",
            ChildRewardsCardBg
        )
        else -> Triple(
            Icons.Default.EmojiEvents,
            "Step $stepNumber guidance",
            ChildDressingCardBg
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(230.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(bgTint.copy(alpha = 0.35f))
            .border(1.2.dp, themeColor.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!mediaUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context)
                    .data(mediaUrl)
                    .crossfade(300)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = actionBadgeText,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            color = themeColor,
                            modifier = Modifier.size(36.dp),
                            strokeWidth = 3.dp
                        )
                    }
                },
                error = {
                    StepVectorFallbackContent(
                        illustrationIcon = illustrationIcon,
                        actionBadgeText = actionBadgeText,
                        scalePulse = scalePulse,
                        themeColor = themeColor
                    )
                },
                success = {
                    SubcomposeAsyncImageContent()
                }
            )

            // Step Action Badge overlay at the bottom
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White.copy(alpha = 0.92f),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = actionBadgeText,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelforaTextPrimary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        } else {
            StepVectorFallbackContent(
                illustrationIcon = illustrationIcon,
                actionBadgeText = actionBadgeText,
                scalePulse = scalePulse,
                themeColor = themeColor
            )
        }
    }
}

@Composable
private fun StepVectorFallbackContent(
    illustrationIcon: ImageVector,
    actionBadgeText: String,
    scalePulse: Float,
    themeColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = themeColor.copy(alpha = 0.15f),
            modifier = Modifier
                .size(86.dp)
                .scale(scalePulse)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = illustrationIcon,
                    contentDescription = actionBadgeText,
                    tint = themeColor,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Text(
                text = actionBadgeText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaTextPrimary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}
