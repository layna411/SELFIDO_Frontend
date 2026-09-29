
package com.simats.selfora.ui.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.theme.*

/**
 * Real-time Step-Specific Visual Guidance Card for Boy 👦 & Girl 👧 OT Dressing Steps.
 * Matches exact step descriptions (e.g. Taking dress from cupboard, arms in sleeve, head through neck).
 */
@Composable
fun VisualStepGuidanceCard(
    stepNumber: Int,
    totalSteps: Int,
    stepTitle: String,
    childInstruction: String,
    gender: String = "BOY", // BOY or GIRL
    onSpeakInstruction: () -> Unit
) {
    val isGirlMode = gender.uppercase().contains("GIRL") || gender.uppercase().contains("FEMALE")
    val cardBg = if (isGirlMode) ChildRewardsCardBg else ChildDressingCardBg
    val themeColor = if (isGirlMode) SelforaSecondary else SelforaPrimary

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
                    Text(
                        text = if (isGirlMode) "Frock Dressing Step $stepNumber of $totalSteps" else "T-Shirt Dressing Step $stepNumber of $totalSteps",
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
                        text = if (isGirlMode) "Girl Mode 👧" else "Boy Mode 👦",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = SelforaTextSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Real-time Step-Specific Visual Illustration Box
            StepVisualIllustrationView(
                stepNumber = stepNumber,
                stepTitle = stepTitle,
                gender = gender,
                themeColor = themeColor
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
                            text = "SPOKEN INSTRUCTION:",
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
 * Renders step-matching visual guidance (Cupboard, T-Shirt, Arms, Head, Waist) for Boy and Girl.
 */
@Composable
fun StepVisualIllustrationView(
    stepNumber: Int,
    stepTitle: String,
    gender: String,
    themeColor: Color
) {
    val isGirl = gender.uppercase().contains("GIRL") || gender.uppercase().contains("FEMALE")
    val titleLower = stepTitle.lowercase()

    // Pulse Animation for active visual indicator
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

    val (illustrationIcon, actionBadgeText, bgTint) = when {
        // Step 1: Cupboard / Wardrobe selection
        titleLower.contains("cupboard") || titleLower.contains("look at") || stepNumber == 1 -> Triple(
            Icons.Default.DoorSliding,
            if (isGirl) "Girl 👧 taking Frock from Cupboard 🚪" else "Boy 👦 taking T-Shirt from Cupboard 🚪",
            if (isGirl) ChildRewardsCardBg else ChildDressingCardBg
        )
        // Step 2 & 3: Picking dress / Tag / Front-Back
        titleLower.contains("pick up") || titleLower.contains("tag") || titleLower.contains("front") || stepNumber == 2 || stepNumber == 3 -> Triple(
            Icons.Default.Checkroom,
            if (isGirl) "Holding Frock & checking tag 🏷️" else "Holding T-Shirt & checking tag 🏷️",
            if (isGirl) ChildEatingCardBg else ChildDressingCardBg
        )
        // Step 4: Neck opening / Collar
        titleLower.contains("neck") || titleLower.contains("collar") || stepNumber == 4 -> Triple(
            Icons.Default.CropFree,
            "Finding & stretching neck hole ⭕",
            ChildEatingCardBg
        )
        // Step 5: Head through neck hole
        titleLower.contains("head") || titleLower.contains("peekaboo") || stepNumber == 5 || stepNumber == 6 || stepNumber == 7 -> Triple(
            Icons.Default.Face,
            if (isGirl) "Ananya popping head through frock! 👧✨" else "Aarav popping head through shirt! 👦✨",
            ChildShoesCardBg
        )
        // Step 6 & 7 & 9 & 10: Right / Left Arm in sleeve
        titleLower.contains("arm") || titleLower.contains("sleeve") -> Triple(
            Icons.Default.PanTool,
            "Pushing arm through sleeve 👕💪",
            ChildDressingCardBg
        )
        // Step 8 / 15 / 16: Pulling shirt / frock down
        titleLower.contains("pull") || titleLower.contains("down") || titleLower.contains("front") -> Triple(
            Icons.Default.ArrowDownward,
            "Pulling shirt/frock smooth down to waist 🔽",
            ChildShoesCardBg
        )
        // Completion step: Fully dressed
        else -> Triple(
            Icons.Default.EmojiEvents,
            if (isGirl) "Girl 👧 completely dressed up! 🎉" else "Boy 👦 completely dressed up! 🎉",
            ChildRewardsCardBg
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgTint.copy(alpha = 0.5f))
            .border(1.dp, themeColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
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
                color = Color.White
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
}
