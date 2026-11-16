package com.simats.selfora.ui.splash

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.HapticFeedbackConstants
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.rounded.Checkroom
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.simats.selfora.R
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Animated Full-Screen Splash Screen for SELFIDO.
 *
 * Features:
 * - Center-Crop edge-to-edge video playback (NO top/bottom mismatch bars on any device)
 * - Muted audio (zero music/sound as requested)
 * - Playful floating sparkles and stars floating up around the waving toddlers
 * - Floating cute ADL badges (Dressing Jacket, Feeding Spoon, Superstar)
 * - Interactive tap star particle bursts (delightful for children!)
 * - Official SELFIDO branding: "Small Steps, Great Growth • சிறிய படிகள், சிறந்த வளர்ச்சி!"
 * - "Skip" pill and bouncing "Let's Go! 🚀" action button
 */
@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    // Prevent double invocation
    var hasCompleted by remember { mutableStateOf(false) }
    fun triggerComplete() {
        if (!hasCompleted) {
            hasCompleted = true
            onSplashComplete()
        }
    }

    // Tap Particle Bursts
    val touchParticles = remember { mutableStateListOf<SplashTouchParticle>() }

    // Infinite animation transitions
    val infiniteTransition = rememberInfiniteTransition(label = "SplashOverlayAnim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOffset"
    )

    // Stars & Sparkles Background Data
    val stars = remember {
        List(20) { index ->
            SplashStarItem(
                id = index,
                initialXFraction = Random.nextFloat(),
                initialYFraction = Random.nextFloat(),
                radius = Random.nextFloat() * 4f + 2f,
                speed = Random.nextFloat() * 0.35f + 0.15f,
                color = when (index % 5) {
                    0 -> Color(0xFFFDE047) // Warm Gold
                    1 -> SelfidoTeal       // Mint Green
                    2 -> SelfidoPurple     // Soft Purple
                    3 -> Color(0xFFF472B6) // Blossom Pink
                    else -> Color(0xFF38BDF8) // Sky Blue
                }
            )
        }
    }

    // Show skip button after a short moment (1.0s)
    var showControls by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(1000)
        showControls = true
        // Fallback safety timeout if video stalls
        delay(5500)
        triggerComplete()
    }

    // Interactive Tap Handler: Spawns celebratory sparkles where tapped
    fun handleUserTap(tapOffset: Offset) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        val colors = listOf(
            SelfidoPurple, SelfidoTeal, Color(0xFFF59E0B),
            Color(0xFFEC4899), Color(0xFF38BDF8), Color(0xFFFDE047)
        )
        val newParticles = List(12) { i ->
            val angle = (i * (360f / 12f) + Random.nextFloat() * 15f) * (PI / 180f).toFloat()
            val speed = Random.nextFloat() * 130f + 50f
            SplashTouchParticle(
                id = System.nanoTime() + i,
                startX = tapOffset.x,
                startY = tapOffset.y,
                dx = cos(angle) * speed,
                dy = sin(angle) * speed,
                color = colors[i % colors.size],
                size = Random.nextFloat() * 5f + 4f
            )
        }
        touchParticles.addAll(newParticles)
    }

    // Clean up particles
    LaunchedEffect(touchParticles.size) {
        if (touchParticles.isNotEmpty()) {
            delay(500)
            touchParticles.clear()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFE8E1)) // Matching neutral tone of the video
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { offset ->
                        handleUserTap(offset)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // -----------------------------------------------------------------
        // 1. True Center-Crop Edge-to-Edge Hardware Video Playback (Muted)
        // -----------------------------------------------------------------
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val textureView = TextureView(ctx)
                var mediaPlayer: MediaPlayer? = null

                fun applyCenterCrop(mp: MediaPlayer, w: Int, h: Int) {
                    val videoWidth = mp.videoWidth.toFloat()
                    val videoHeight = mp.videoHeight.toFloat()
                    val viewWidth = w.toFloat()
                    val viewHeight = h.toFloat()

                    if (videoWidth > 0 && videoHeight > 0 && viewWidth > 0 && viewHeight > 0) {
                        val scaleX: Float
                        val scaleY: Float
                        if (viewWidth / viewHeight > videoWidth / videoHeight) {
                            scaleX = 1f
                            scaleY = (viewWidth / videoWidth * videoHeight) / viewHeight
                        } else {
                            scaleX = (viewHeight / videoHeight * videoWidth) / viewWidth
                            scaleY = 1f
                        }
                        val matrix = Matrix()
                        matrix.setScale(scaleX, scaleY, viewWidth / 2f, viewHeight / 2f)
                        textureView.setTransform(matrix)
                    }
                }

                textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                        val s = Surface(surface)
                        mediaPlayer = MediaPlayer().apply {
                            setSurface(s)
                            try {
                                val afd = ctx.resources.openRawResourceFd(R.raw.splash_video)
                                if (afd != null) {
                                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                    afd.close()
                                } else {
                                    setDataSource(ctx, Uri.parse("android.resource://${ctx.packageName}/${R.raw.splash_video}"))
                                }
                            } catch (e: Exception) {
                                setDataSource(ctx, Uri.parse("android.resource://${ctx.packageName}/${R.raw.splash_video}"))
                            }

                            // Completely mute video (remove music as requested)
                            setVolume(0f, 0f)
                            isLooping = false

                            setOnPreparedListener { mp ->
                                applyCenterCrop(mp, width, height)
                                mp.start()
                            }
                            setOnCompletionListener {
                                triggerComplete()
                            }
                            setOnErrorListener { _, _, _ ->
                                triggerComplete()
                                true
                            }
                            prepareAsync()
                        }
                    }

                    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                        mediaPlayer?.let { mp ->
                            applyCenterCrop(mp, width, height)
                        }
                    }

                    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                        mediaPlayer?.release()
                        mediaPlayer = null
                        return true
                    }

                    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
                }
                textureView
            }
        )

        // -----------------------------------------------------------------
        // 2. Playful Floating Sparkles & Interactive Tap Particles Overlay
        // -----------------------------------------------------------------
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            stars.forEach { star ->
                val dynamicY = ((star.initialYFraction * height - (waveOffset * 25f * star.speed)) % height + height) % height
                val dynamicX = star.initialXFraction * width + sin(waveOffset + star.id) * 10f
                val alphaTwinkle = 0.35f + 0.45f * sin(waveOffset * 2f + star.id).coerceIn(0f, 1f)

                drawStarSparkle(
                    center = Offset(dynamicX, dynamicY),
                    radius = star.radius,
                    color = star.color.copy(alpha = alphaTwinkle)
                )
            }

            // Draw Tap-Burst Confetti Particles
            touchParticles.forEach { p ->
                drawCircle(
                    color = p.color,
                    radius = p.size,
                    center = Offset(p.startX + p.dx * 0.4f, p.startY + p.dy * 0.4f)
                )
            }
        }

        // -----------------------------------------------------------------
        // 3. Floating Cute ADL Mini Badges (Dressing & Feeding Autonomy)
        // -----------------------------------------------------------------
        // Floating Dressing Badge (Top-Left)
        FloatingSplashBadge(
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(top = 50.dp, start = 16.dp)
                .offset(y = (sin(waveOffset) * 6f).dp),
            bgColor = Color(0xFFF3E8FF),
            icon = Icons.Rounded.Checkroom,
            iconTint = SelfidoPurple,
            sparkle = "✨"
        )

        // Floating Feeding Spoon Badge (Top-Right under skip)
        FloatingSplashBadge(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 90.dp, end = 16.dp)
                .offset(y = (cos(waveOffset) * 6f).dp),
            bgColor = Color(0xFFD1FAE5),
            icon = Icons.Rounded.Restaurant,
            iconTint = SelfidoTeal,
            sparkle = "🥄"
        )

        // -----------------------------------------------------------------
        // 4. Top-Right "Skip ➔" Pill Button
        // -----------------------------------------------------------------
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(400)) + slideInVertically(initialOffsetY = { -30 }),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 18.dp)
        ) {
            Surface(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    triggerComplete()
                },
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.9f)),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Skip",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelfidoPurple
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Skip Splash",
                        tint = SelfidoPurple,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // -----------------------------------------------------------------
        // 5. Bottom "Let's Go! 🚀" Action Control
        // -----------------------------------------------------------------
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { 60 }),
            exit = fadeOut(animationSpec = tween(200)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 36.dp)
        ) {
            Button(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                    triggerComplete()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SelfidoPurple
                ),
                shape = RoundedCornerShape(26.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp, pressedElevation = 2.dp),
                contentPadding = PaddingValues(horizontal = 34.dp, vertical = 13.dp),
                modifier = Modifier
                    .scale(pulseScale)
                    .shadow(12.dp, RoundedCornerShape(26.dp), ambientColor = SelfidoPurple.copy(alpha = 0.40f))
            ) {
                Text(
                    text = "Let's Go! 🚀",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Cute Floating ADL mini badge.
 */
@Composable
private fun FloatingSplashBadge(
    modifier: Modifier = Modifier,
    bgColor: Color,
    icon: ImageVector,
    iconTint: Color,
    sparkle: String
) {
    Surface(
        modifier = modifier.shadow(4.dp, CircleShape),
        shape = CircleShape,
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color.White)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = sparkle,
                fontSize = 9.sp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
            )
        }
    }
}

/**
 * Helper to draw a crisp 4-pointed star sparkle on Compose Canvas.
 */
private fun DrawScope.drawStarSparkle(
    center: Offset,
    radius: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(center.x, center.y - radius * 1.5f)
        cubicTo(center.x, center.y, center.x, center.y, center.x + radius * 1.5f, center.y)
        cubicTo(center.x, center.y, center.x, center.y, center.x + radius * 1.5f, center.y)
        cubicTo(center.x, center.y, center.x, center.y, center.x - radius * 1.5f, center.y)
        cubicTo(center.x, center.y, center.x, center.y, center.x, center.y - radius * 1.5f)
        close()
    }
    drawPath(path = path, color = color)
}

/**
 * Background twinkling star item.
 */
private data class SplashStarItem(
    val id: Int,
    val initialXFraction: Float,
    val initialYFraction: Float,
    val radius: Float,
    val speed: Float,
    val color: Color
)

/**
 * Interactive touch particle data.
 */
private data class SplashTouchParticle(
    val id: Long,
    val startX: Float,
    val startY: Float,
    val dx: Float,
    val dy: Float,
    val color: Color,
    val size: Float
)
