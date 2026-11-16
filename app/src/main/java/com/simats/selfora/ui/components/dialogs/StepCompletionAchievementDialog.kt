package com.simats.selfora.ui.components.dialogs

import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.simats.selfora.R
import com.simats.selfora.ui.theme.*

@Composable
fun StepCompletionAchievementDialog(
    stepNumber: Int,
    totalSteps: Int,
    stepTitle: String,
    isFinalStep: Boolean = false,
    isTamil: Boolean = false,
    onContinue: () -> Unit
) {
    val context = LocalContext.current

    // Play celebratory sound upon completion dialog open
    DisposableEffect(Unit) {
        var audioPlayer: MediaPlayer? = null
        try {
            audioPlayer = MediaPlayer.create(context, R.raw.celebration_sound)?.apply {
                setVolume(1.0f, 1.0f)
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        onDispose {
            try {
                audioPlayer?.stop()
                audioPlayer?.release()
            } catch (_: Exception) {}
            audioPlayer = null
        }
    }

    // Pulsing star & badge animation
    val infiniteTransition = rememberInfiniteTransition(label = "AchievementAnim")
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starScale"
    )
    val buttonPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "buttonPulse"
    )

    Dialog(
        onDismissRequest = onContinue,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .wrapContentHeight()
                    .shadow(24.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFFAF5FF),
                                    Color(0xFFFFFFFF),
                                    Color(0xFFF3E8FF)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Celebration Stars & Badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier
                                .size(28.dp)
                                .scale(starScale)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SelfidoPurple.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SelfidoPurple.copy(alpha = 0.25f))
                        ) {
                            Text(
                                text = if (isFinalStep) {
                                    if (isTamil) "🏆 செயல்பாடு முடிந்தது! 🏆" else "🏆 ACTIVITY COMPLETED! 🏆"
                                } else {
                                    if (isTamil) "⭐ படி முடிந்தது! ⭐" else "⭐ STEP COMPLETED! ⭐"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SelfidoPurple,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier
                                .size(28.dp)
                                .scale(starScale)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Celebrating Toddler Boy Video Player Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(2.dp, Brush.linearGradient(listOf(SelfidoPurple, SelfidoTeal)), RoundedCornerShape(24.dp))
                            .background(Color(0xFFF3E8FF)),
                        contentAlignment = Alignment.Center
                    ) {
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
                                                val afd = ctx.resources.openRawResourceFd(R.raw.step_completion_celebration)
                                                if (afd != null) {
                                                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                                    afd.close()
                                                } else {
                                                    setDataSource(ctx, Uri.parse("android.resource://${ctx.packageName}/${R.raw.step_completion_celebration}"))
                                                }
                                            } catch (_: Exception) {
                                                setDataSource(ctx, Uri.parse("android.resource://${ctx.packageName}/${R.raw.step_completion_celebration}"))
                                            }

                                            // Seamless child celebrating loop
                                            isLooping = true
                                            setVolume(1.0f, 1.0f)

                                            setOnPreparedListener { mp ->
                                                applyCenterCrop(mp, width, height)
                                                mp.start()
                                            }
                                            prepareAsync()
                                        }
                                    }

                                    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
                                        mediaPlayer?.let { mp -> applyCenterCrop(mp, width, height) }
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

                        // Floating Star Badge Overlay
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("⭐+1", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step Title & Praise
                    Text(
                        text = if (isTamil) "அருமை! படி $stepNumber முடிந்தது! 🎉" else "Awesome Job! Step $stepNumber Done! 🎉",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SelforaTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isFinalStep) {
                            if (isTamil) "முழு செயல்பாட்டையும் வெற்றிகரமாக முடித்துவிட்டாய்! 🌟" else "You completed the entire activity like a superstar! 🌟"
                        } else {
                            if (isTamil) "சிறந்த முயற்சி! அடுத்த படியை இப்போது தொடங்குவோம்!" else "Great independence! Let's conquer the next step!"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SelforaTextSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Big Action Button
                    Button(
                        onClick = onContinue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .scale(buttonPulse),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFinalStep) SelfidoPurple else ChildGreenPlay
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isFinalStep) Icons.Default.EmojiEvents else Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isFinalStep) {
                                    if (isTamil) "பரிசைப் பெற்று முடி! 🏆" else "Collect Trophy & Finish! 🏆"
                                } else {
                                    if (isTamil) "அடுத்த படிக்கு செல் ➔ 🚀" else "Next Step ➔ 🚀"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
