package com.simats.selfora.ui.components.avatar

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.simats.selfora.R

enum class ChildAvatarType(
    val id: String,
    val characterName: String,
    val gender: String,
    val ageDescription: String,
    val defaultEmoji: String
) {
    BOY("aarav_boy", "Aarav", "BOY", "7 yrs", "👦"),
    GIRL("ananya_girl", "Ananya", "GIRL", "6 yrs", "👧");

    companion object {
        fun fromIdOrDefault(id: String?, gender: String? = null): ChildAvatarType {
            return when {
                id == "ananya_girl" || id == "girl_avatar" || id?.contains("ananya", ignoreCase = true) == true || gender.equals("GIRL", ignoreCase = true) -> GIRL
                else -> BOY
            }
        }
    }
}

enum class AvatarPose {
    STANDARD,
    WAVE,
    CELEBRATION
}

enum class AvatarBackgroundStyle {
    NONE,
    CIRCLE_GLASS,
    SOFT_GRADIENT,
    BORDERED_CIRCLE
}

@Composable
fun ChildAvatar(
    avatarType: ChildAvatarType = ChildAvatarType.BOY,
    avatarSize: Dp = 64.dp,
    avatarPose: AvatarPose = AvatarPose.STANDARD,
    backgroundStyle: AvatarBackgroundStyle = AvatarBackgroundStyle.BORDERED_CIRCLE,
    contentDescription: String? = avatarType.characterName,
    modifier: Modifier = Modifier
) {
    val drawableRes = when (avatarType) {
        ChildAvatarType.BOY -> when (avatarPose) {
            AvatarPose.STANDARD -> R.drawable.boy_avatar
            AvatarPose.WAVE -> R.drawable.boy_avatar_wave
            AvatarPose.CELEBRATION -> R.drawable.boy_avatar_celebration
        }
        ChildAvatarType.GIRL -> when (avatarPose) {
            AvatarPose.STANDARD -> R.drawable.girl_avatar
            AvatarPose.WAVE -> R.drawable.girl_avatar_wave
            AvatarPose.CELEBRATION -> R.drawable.girl_avatar_celebration
        }
    }

    val isWavingOrCeleb = avatarPose == AvatarPose.WAVE || avatarPose == AvatarPose.CELEBRATION
    val infiniteTransition = rememberInfiniteTransition(label = "AvatarMotion")
    val scale by if (isWavingOrCeleb) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.03f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "AvatarScale"
        )
    } else {
        remember { mutableStateOf(1.0f) }
    }

    val borderBrush = if (avatarType == ChildAvatarType.GIRL) {
        Brush.sweepGradient(listOf(Color(0xFF9333EA), Color(0xFFC084FC), Color(0xFF9333EA)))
    } else {
        Brush.sweepGradient(listOf(Color(0xFF2563EB), Color(0xFF60A5FA), Color(0xFF2563EB)))
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(avatarSize)
            .scale(scale)
    ) {
        when (backgroundStyle) {
            AvatarBackgroundStyle.NONE -> {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }
            AvatarBackgroundStyle.BORDERED_CIRCLE -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(2.5.dp, borderBrush, CircleShape)
                        .background(if (avatarType == ChildAvatarType.GIRL) Color(0xFFF3E8FF) else Color(0xFFEFF6FF))
                ) {
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            AvatarBackgroundStyle.CIRCLE_GLASS -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                if (avatarType == ChildAvatarType.GIRL)
                                    listOf(Color(0xFFF3E8FF), Color(0xFFE9D5FF))
                                else
                                    listOf(Color(0xFFEFF6FF), Color(0xFFDBEAFE))
                            )
                        )
                ) {
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            AvatarBackgroundStyle.SOFT_GRADIENT -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.verticalGradient(
                                if (avatarType == ChildAvatarType.GIRL)
                                    listOf(Color(0xFFF3E8FF).copy(alpha = 0.8f), Color(0xFFE9D5FF).copy(alpha = 0.5f))
                                else
                                    listOf(Color(0xFFEFF6FF).copy(alpha = 0.8f), Color(0xFFDBEAFE).copy(alpha = 0.5f))
                            )
                        )
                ) {
                    Image(
                        painter = painterResource(id = drawableRes),
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun ChildAvatarImage(
    avatarId: String?,
    gender: String? = "BOY",
    size: Dp = 64.dp,
    showBorder: Boolean = true,
    animateWave: Boolean = false,
    modifier: Modifier = Modifier
) {
    val type = ChildAvatarType.fromIdOrDefault(avatarId, gender)
    val pose = if (animateWave) AvatarPose.WAVE else AvatarPose.STANDARD
    val bg = if (showBorder) AvatarBackgroundStyle.BORDERED_CIRCLE else AvatarBackgroundStyle.NONE
    ChildAvatar(
        avatarType = type,
        avatarSize = size,
        avatarPose = pose,
        backgroundStyle = bg,
        contentDescription = type.characterName,
        modifier = modifier
    )
}
