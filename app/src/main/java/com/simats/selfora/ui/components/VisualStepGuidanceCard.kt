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

    // Auto-resolve GIF for Boy T-Shirt (1..20), Boy Shirt (1..23), or Boy Jacket (1..26) if mediaUrl is not explicitly supplied
    val resolvedMediaUrl = if (!mediaUrl.isNullOrBlank()) {
        mediaUrl
    } else if (!isGirlMode && (activityTitle.contains("Jacket", ignoreCase = true) || activityTitle.contains("ஜாக்கெட்")) && stepNumber in 1..26) {
        "file:///android_asset/dressing/boy/jacket/step_${String.format("%02d", stepNumber)}.gif"
    } else if (!isGirlMode && (activityTitle.contains("Shirt", ignoreCase = true) && !activityTitle.contains("T-Shirt", ignoreCase = true) && !activityTitle.contains("T-shirt", ignoreCase = true) && !activityTitle.contains("டி-சர்ட்")) && stepNumber in 1..23) {
        "file:///android_asset/dressing/boy/shirt/step_${String.format("%02d", stepNumber)}.gif"
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

    // Accurate step mapping with vector icons and child-friendly action descriptors
    val (illustrationIcon, actionBadgeText, bgTint) = when {
        // --- BOY ZIP-UP JACKET SPECIFIC STEPS ---
        titleLower.contains("slider") && titleLower.contains("find") -> Triple(
            Icons.Default.Search,
            "Finding the zipper slider at bottom 🔍",
            ChildRewardsCardBg
        )
        titleLower.contains("insertion pin") && titleLower.contains("find") -> Triple(
            Icons.Default.Search,
            "Finding the zipper insertion pin 📍",
            ChildRewardsCardBg
        )
        titleLower.contains("slider") && titleLower.contains("hold") -> Triple(
            Icons.Default.TouchApp,
            "Holding zipper slider steady 🤏",
            ChildRewardsCardBg
        )
        titleLower.contains("insertion pin") && titleLower.contains("hold") -> Triple(
            Icons.Default.TouchApp,
            "Holding insertion pin with other hand 🤌",
            ChildRewardsCardBg
        )
        titleLower.contains("insert the pin") || (titleLower.contains("pin") && titleLower.contains("slider")) -> Triple(
            Icons.Default.CheckCircle,
            "Clicking pin fully into slider! Click! 🔒",
            ChildRewardsCardBg
        )
        titleLower.contains("bottom of the jacket") || (titleLower.contains("bottom") && titleLower.contains("steady")) -> Triple(
            Icons.Default.FrontHand,
            "Holding bottom of jacket steady 🧥⚓",
            ChildRewardsCardBg
        )
        titleLower.contains("slider upward") || (titleLower.contains("zipper") && titleLower.contains("upward")) -> Triple(
            Icons.Default.ArrowUpward,
            "Zipping upward! Pull up! ⬆️",
            ChildRewardsCardBg
        )
        titleLower.contains("chest") && titleLower.contains("zipper") -> Triple(
            Icons.Default.ArrowUpward,
            "Zipping up past chest level ⬆️",
            ChildRewardsCardBg
        )
        titleLower.contains("neck") && titleLower.contains("zipper") -> Triple(
            Icons.Default.ArrowUpward,
            "Zipping all the way to neck! ⬆️",
            ChildRewardsCardBg
        )
        titleLower.contains("comfortably closed") || titleLower.contains("stop when") -> Triple(
            Icons.Default.CheckCircle,
            "Stopping zipper at comfortable height ✔️",
            ChildRewardsCardBg
        )
        titleLower.contains("release") && titleLower.contains("zipper") -> Triple(
            Icons.Default.Done,
            "Releasing zipper pull tab 👍",
            ChildRewardsCardBg
        )
        titleLower.contains("front sides") && titleLower.contains("together") -> Triple(
            Icons.Default.ViewAgenda,
            "Bringing both front sides together 🧥",
            ChildDressingCardBg
        )
        titleLower.contains("jacket") && (titleLower.contains("stand") || titleLower.contains("sit in front")) -> Triple(
            Icons.Default.AccessibilityNew,
            "Boy 👦 standing calmly in front of Jacket 🧥",
            ChildDressingCardBg
        )
        titleLower.contains("jacket") && titleLower.contains("look at") -> Triple(
            Icons.Default.Visibility,
            "Examining open jacket & zipper 👀🧥",
            ChildDressingCardBg
        )
        titleLower.contains("jacket") && titleLower.contains("pick up") -> Triple(
            Icons.Default.FrontHand,
            "Picking up jacket with both hands 🤲🧥",
            ChildEatingCardBg
        )
        titleLower.contains("jacket") && (titleLower.contains("front and back") || titleLower.contains("front with")) -> Triple(
            Icons.Default.Checkroom,
            "Finding front zipper & back panel 🏷️🧥",
            ChildDressingCardBg
        )
        titleLower.contains("jacket") && titleLower.contains("collar") -> Triple(
            Icons.Default.CropFree,
            "Locating jacket collar at the top 🧥",
            ChildDressingCardBg
        )
        titleLower.contains("jacket") && (titleLower.contains("by the shoulders") || (titleLower.contains("hold") && titleLower.contains("shoulder"))) -> Triple(
            Icons.Default.PanTool,
            "Holding jacket by both shoulder seams 🧥💪",
            ChildDressingCardBg
        )
        titleLower.contains("jacket") && titleLower.contains("onto both shoulders") -> Triple(
            Icons.Default.AccessibilityNew,
            "Settling jacket onto both shoulders 🧥✨",
            ChildDressingCardBg
        )
        titleLower.contains("jacket") && titleLower.contains("straighten") && titleLower.contains("shoulders") && !titleLower.contains("waist") -> Triple(
            Icons.Default.AutoFixHigh,
            "Straightening jacket at shoulders 🧥✨",
            ChildEatingCardBg
        )
        titleLower.contains("jacket") && (titleLower.contains("shoulders and waist") || titleLower.contains("waist")) -> Triple(
            Icons.Default.AutoFixHigh,
            "Smoothing jacket around shoulders & waist ✨",
            ChildEatingCardBg
        )
        stepNumber == 26 || (titleLower.contains("jacket") && (titleLower.contains("comfortable") || titleLower.contains("positioned"))) -> Triple(
            Icons.Default.EmojiEvents,
            "Boy 👦 handsome and warm in Zip-Up Jacket! 🎉🧥⭐",
            ChildRewardsCardBg
        )

        // --- BOY SHIRT (BUTTON-UP) SPECIFIC STEPS ---
        titleLower.contains("top button") || (titleLower.contains("button") && titleLower.contains("find")) -> Triple(
            Icons.Default.RadioButtonChecked,
            "Finding top button & buttonhole 🔘🔍",
            ChildRewardsCardBg
        )
        titleLower.contains("buttonhole") && (titleLower.contains("hold") || titleLower.contains("hand")) -> Triple(
            Icons.Default.TouchApp,
            "Holding buttonhole with one hand 🤏",
            ChildRewardsCardBg
        )
        titleLower.contains("button") && (titleLower.contains("hold") || titleLower.contains("other hand")) -> Triple(
            Icons.Default.TouchApp,
            "Holding button with other hand 🤌",
            ChildRewardsCardBg
        )
        titleLower.contains("push") && titleLower.contains("button") -> Triple(
            Icons.Default.CheckCircle,
            "Pushing button through buttonhole 🔘➡️",
            ChildRewardsCardBg
        )
        titleLower.contains("secured") || titleLower.contains("button is secured") -> Triple(
            Icons.Default.CheckCircle,
            "Checking button is fastened securely ✔️",
            ChildRewardsCardBg
        )
        titleLower.contains("repeat") && titleLower.contains("button") -> Triple(
            Icons.Default.Done,
            "Buttoning down from top to bottom 🔘⬇️",
            ChildRewardsCardBg
        )
        titleLower.contains("fastened") || titleLower.contains("required buttons") -> Triple(
            Icons.Default.Done,
            "All buttons fastened neatly 🔘👔✔️",
            ChildRewardsCardBg
        )
        titleLower.contains("collar") && (titleLower.contains("straighten") || titleLower.contains("fix")) -> Triple(
            Icons.Default.Checkroom,
            "Straightening collar neatly around neck 👔✨",
            ChildEatingCardBg
        )
        titleLower.contains("collar") || titleLower.contains("collar/neck") -> Triple(
            Icons.Default.CropFree,
            "Finding collar & neck opening 👔",
            ChildDressingCardBg
        )
        titleLower.contains("one arm") || (titleLower.contains("arm into") && !titleLower.contains("other")) -> Triple(
            Icons.Default.PanTool,
            "Sliding first arm into sleeve 💪👔",
            ChildDressingCardBg
        )
        titleLower.contains("other arm") -> Triple(
            Icons.Default.PanTool,
            "Sliding other arm into sleeve 💪👔",
            ChildDressingCardBg
        )
        titleLower.contains("second sleeve") -> Triple(
            Icons.Default.ArrowUpward,
            "Pulling second sleeve onto shoulder 👕",
            ChildDressingCardBg
        )
        titleLower.contains("sleeve") && titleLower.contains("toward the shoulder") -> Triple(
            Icons.Default.ArrowUpward,
            "Pulling sleeve up onto shoulder 👕",
            ChildDressingCardBg
        )
        titleLower.contains("onto both shoulders") -> Triple(
            Icons.Default.AccessibilityNew,
            "Settling shirt onto both shoulders 👔✨",
            ChildDressingCardBg
        )
        titleLower.contains("straighten") && titleLower.contains("shoulders") -> Triple(
            Icons.Default.AutoFixHigh,
            "Straightening shirt at the shoulders 👔✨",
            ChildEatingCardBg
        )
        titleLower.contains("front panels") && titleLower.contains("down") -> Triple(
            Icons.Default.ArrowDownward,
            "Pulling front panels down flat 🔽",
            ChildShoesCardBg
        )
        titleLower.contains("sleeves and front") -> Triple(
            Icons.Default.AutoFixHigh,
            "Smoothing sleeves & front panels ✨",
            ChildEatingCardBg
        )
        titleLower.contains("waist") && titleLower.contains("shirt down") -> Triple(
            Icons.Default.ArrowDownward,
            "Pulling shirt down to waist 🔽",
            ChildShoesCardBg
        )
        stepNumber == 23 || (titleLower.contains("positioned and comfortable") && titleLower.contains("shirt")) -> Triple(
            Icons.Default.EmojiEvents,
            "Boy 👦 handsome and neatly dressed in Shirt! 🎉👔⭐",
            ChildRewardsCardBg
        )

        // --- BOY T-SHIRT & GENERAL DRESSING STEPS ---
        stepNumber == 1 || titleLower.contains("stand") || titleLower.contains("sit in front") -> Triple(
            Icons.Default.AccessibilityNew,
            if (isGirl) "Girl 👧 standing calmly in front of Frock" else "Boy 👦 standing calmly in front of T-Shirt 👕",
            if (isGirl) ChildRewardsCardBg else ChildDressingCardBg
        )
        stepNumber == 2 || titleLower.contains("look at") -> Triple(
            Icons.Default.Visibility,
            if (isGirl) "Examining the Frock carefully 👧✨" else if (titleLower.contains("t-shirt") || titleLower.contains("tshirt")) "Examining the T-shirt carefully 👀👕" else "Examining button-up shirt 👀👔",
            if (isGirl) ChildRewardsCardBg else ChildDressingCardBg
        )
        stepNumber == 3 || titleLower.contains("pick up") -> Triple(
            Icons.Default.FrontHand,
            if (isGirl) "Picking up Frock with both hands 🤲" else if (titleLower.contains("t-shirt") || titleLower.contains("tshirt")) "Picking up T-shirt with both hands 🤲👕" else "Picking up shirt with both hands 🤲👔",
            ChildEatingCardBg
        )
        stepNumber == 4 || (titleLower.contains("front") && titleLower.contains("back")) -> Triple(
            Icons.Default.Checkroom,
            if (titleLower.contains("t-shirt") || titleLower.contains("tshirt")) "Finding back collar tag & front graphic 🏷️" else "Finding collar & front buttons 🏷️🔘",
            ChildDressingCardBg
        )
        stepNumber == 5 || titleLower.contains("bottom edge") || titleLower.contains("bottom opening") -> Triple(
            Icons.Default.South,
            "Locating bottom hem opening 🔽",
            ChildEatingCardBg
        )
        (titleLower.contains("head") && titleLower.contains("neck")) || titleLower.contains("head through") -> Triple(
            Icons.Default.Face,
            if (isGirl) "Ananya popping head through frock! 👧✨" else "Aarav popping head through neck hole! 👦✨",
            ChildShoesCardBg
        )
        stepNumber == 6 || titleLower.contains("neck opening") || titleLower.contains("neck hole") -> Triple(
            Icons.Default.CropFree,
            "Finding the round neck opening ⭕",
            ChildEatingCardBg
        )
        stepNumber == 7 || titleLower.contains("shoulder areas") || titleLower.contains("hold") -> Triple(
            Icons.Default.PanTool,
            "Holding shoulder seams firmly 👕💪",
            ChildDressingCardBg
        )
        stepNumber == 8 || titleLower.contains("chest level") || titleLower.contains("lift") -> Triple(
            Icons.Default.North,
            "Lifting T-shirt up to chest level ⬆️",
            ChildShoesCardBg
        )
        stepNumber == 9 -> Triple(
            Icons.Default.Face,
            if (isGirl) "Ananya popping head through frock! 👧✨" else "Aarav popping head through neck hole! 👦✨",
            ChildShoesCardBg
        )
        stepNumber == 10 || (titleLower.contains("pull") && titleLower.contains("over the head")) -> Triple(
            Icons.Default.ArrowDownward,
            "Pulling collar smoothly past head onto neck 🔽",
            ChildShoesCardBg
        )
        stepNumber == 11 || (titleLower.contains("right") && titleLower.contains("find")) -> Triple(
            Icons.Default.Search,
            "Locating right sleeve opening 🔎",
            ChildDressingCardBg
        )
        stepNumber == 12 || (titleLower.contains("right") && titleLower.contains("through")) -> Triple(
            Icons.Default.PanTool,
            "Pushing right arm through right sleeve 💪👕",
            ChildDressingCardBg
        )
        stepNumber == 13 || (titleLower.contains("right") && titleLower.contains("shoulder")) -> Triple(
            Icons.Default.ArrowUpward,
            "Pulling right sleeve securely onto shoulder 👕",
            ChildDressingCardBg
        )
        stepNumber == 14 || (titleLower.contains("left") && titleLower.contains("find")) -> Triple(
            Icons.Default.Search,
            "Locating left sleeve opening 🔎",
            ChildDressingCardBg
        )
        stepNumber == 15 || (titleLower.contains("left") && titleLower.contains("through")) -> Triple(
            Icons.Default.PanTool,
            "Pushing left arm through left sleeve 💪👕",
            ChildDressingCardBg
        )
        stepNumber == 16 || (titleLower.contains("left") && titleLower.contains("shoulder")) -> Triple(
            Icons.Default.ArrowUpward,
            "Pulling left sleeve securely onto shoulder 👕",
            ChildDressingCardBg
        )
        stepNumber == 17 || (titleLower.contains("front") && titleLower.contains("down")) -> Triple(
            Icons.Default.ArrowDownward,
            "Pulling front of shirt down over tummy 🔽",
            ChildShoesCardBg
        )
        stepNumber == 18 || (titleLower.contains("back") && titleLower.contains("down")) -> Triple(
            Icons.Default.ArrowDownward,
            "Pulling back of shirt down over waist 🔽",
            ChildShoesCardBg
        )
        stepNumber == 19 || titleLower.contains("straighten") -> Triple(
            Icons.Default.AutoFixHigh,
            "Straightening fabric at shoulders & waist ✨",
            ChildEatingCardBg
        )
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
