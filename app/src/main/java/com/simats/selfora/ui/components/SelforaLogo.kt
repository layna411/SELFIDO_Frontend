package com.simats.selfora.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.R
import com.simats.selfora.ui.theme.*

/**
 * Official Application Brand Logo (SELFIDO / SELFORA).
 * Pediatric Activities of Daily Living (ADL) & Independence:
 * - Happy smiling child emblem
 * - Purple zipper jacket (Dressing ADL)
 * - Mint-green spoon & bowl with heart (Self-feeding & Care ADL)
 * - Tagline: "Little steps, Big wins"
 */

@Composable
fun SelforaLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    animated: Boolean = true,
    showAura: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SelforaLogoAnim")

    val pulseScale by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(1800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
    } else {
        remember { mutableFloatStateOf(1.0f) }
    }

    val auraAlpha by if (animated && showAura) {
        infiniteTransition.animateFloat(
            initialValue = 0.25f,
            targetValue = 0.55f,
            animationSpec = infiniteRepeatable(
                animation = tween(2200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "auraAlpha"
        )
    } else {
        remember { mutableFloatStateOf(0.35f) }
    }

    val starRotation by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = -3f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(1600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "starRotation"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Box(
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        // Outer Pulsing Glow Aura
        if (showAura) {
            Box(
                modifier = Modifier
                    .size(size * 1.15f)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                SelfidoPurple.copy(alpha = auraAlpha * 0.45f),
                                SelfidoTeal.copy(alpha = auraAlpha * 0.35f),
                                Color(0xFFF59E0B).copy(alpha = auraAlpha * 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Inner Elevated Badge Card displaying the 3D Character Emblem
        Surface(
            modifier = Modifier
                .size(size)
                .scale(pulseScale)
                .rotate(starRotation)
                .shadow(
                    elevation = 6.dp,
                    shape = CircleShape,
                    ambientColor = SelfidoPurple.copy(alpha = 0.25f),
                    spotColor = SelfidoTeal.copy(alpha = 0.30f)
                ),
            shape = CircleShape,
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo_selfido_emblem_sq),
                contentDescription = "SELFIDO Logo Mark",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

/**
 * Reusable SELFIDO / SELFORA Logo Header Banner.
 */
@Composable
fun SelforaLogoBanner(
    modifier: Modifier = Modifier,
    iconSize: Dp = 44.dp,
    showTagline: Boolean = true,
    taglineText: String = "Little steps, Big wins",
    tamilTagline: String? = null,
    isCompact: Boolean = false
) {
    val brandGradient = Brush.horizontalGradient(
        colors = listOf(
            SelfidoPurple,
            Color(0xFF673AB7),
            SelfidoTeal
        )
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        SelforaLogoBadge(
            size = iconSize,
            animated = true,
            showAura = true
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SELFIDO",
                    fontSize = if (isCompact) 20.sp else 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.2.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = brandGradient
                    )
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Glowing heart accent from the logo's 'i'
                Text(
                    text = "💜",
                    fontSize = if (isCompact) 11.sp else 13.sp,
                    modifier = Modifier.offset(y = (-4).dp)
                )
            }

            if (showTagline) {
                Text(
                    text = taglineText,
                    fontSize = if (isCompact) 10.sp else 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = SelforaTextSecondary,
                    letterSpacing = 0.2.sp
                )

                if (tamilTagline != null) {
                    Text(
                        text = tamilTagline,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        color = SelforaTextSecondary.copy(alpha = 0.85f),
                        lineHeight = 11.sp
                    )
                }
            }
        }
    }
}

/**
 * Full 3D Hero Logo Card for Splash & Welcome Screens.
 */
@Composable
fun SelfidoFullHeroCard(
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    animated: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HeroCardAnim")

    val pulseScale by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cardScale"
        )
    } else {
        remember { mutableFloatStateOf(1.0f) }
    }

    val floatOffset by if (animated) {
        infiniteTransition.animateFloat(
            initialValue = -5f,
            targetValue = 5f,
            animationSpec = infiniteRepeatable(
                animation = tween(2400, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "cardFloat"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    Surface(
        modifier = modifier
            .size(size)
            .offset(y = floatOffset.dp)
            .scale(pulseScale)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = SelfidoPurple.copy(alpha = 0.20f),
                spotColor = SelfidoTeal.copy(alpha = 0.30f)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo_selfido),
            contentDescription = "SELFIDO - Little steps, Big wins",
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(28.dp)),
            contentScale = ContentScale.Fit
        )
    }
}
