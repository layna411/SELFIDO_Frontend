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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
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
fun PracticeSessionIntroDialog(
    activityTitle: String = "ADL Practice Session",
    isTamil: Boolean = false,
    onStartPractice: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Background cheerful marimba sound player
    DisposableEffect(Unit) {
        var audioPlayer: MediaPlayer? = null
        try {
            audioPlayer = MediaPlayer.create(context, R.raw.practice_session_sound)?.apply {
                isLooping = true
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

    // Gentle pulsing animation for the "Start Practice" CTA
    val infiniteTransition = rememberInfiniteTransition(label = "PracticeIntroAnim")
    val buttonPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "btnPulse"
    )
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starScale"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.94f)
                    .wrapContentHeight()
                    .shadow(28.dp, RoundedCornerShape(32.dp)),
                shape = RoundedCornerShape(32.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFFF5F3FF), // Soft lavender top
                                    Color(0xFFFFFFFF),
                                    Color(0xFFEDE9FE)  // Lilac glow bottom
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar with Title and Skip/Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier
                                    .size(24.dp)
                                    .scale(starScale)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SelfidoPurple.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, SelfidoPurple.copy(alpha = 0.3f))
                            ) {
                                Text(
                                    text = if (isTamil) "🎒 பயிற்சி நேரம்!" else "🎒 PRACTICE SESSION!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SelfidoPurple,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        // Close / Skip Button
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onDismiss() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = SelforaTextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Video Card: Toddler exploring room and wardrobe with Center-Crop
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .border(
                                width = 2.5.dp,
                                brush = Brush.linearGradient(
                                    listOf(SelfidoPurple, SelfidoTeal, Color(0xFFF59E0B))
                                ),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .background(Color(0xFFEDE9FE)),
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
                                                val afd = ctx.resources.openRawResourceFd(R.raw.practice_intro_video)
                                                if (afd != null) {
                                                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                                    afd.close()
                                                } else {
                                                    setDataSource(ctx, Uri.parse("android.resource://${ctx.packageName}/${R.raw.practice_intro_video}"))
                                                }
                                            } catch (_: Exception) {
                                                setDataSource(ctx, Uri.parse("android.resource://${ctx.packageName}/${R.raw.practice_intro_video}"))
                                            }

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

                        // Floating Cute Badge Overlay
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.55f),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("🎵 Ready to Learn!", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Title & Description
                    Text(
                        text = if (isTamil) "அலமாரியை ஆராய்வோம்! 👕✨" else "Ready to Practice! 👕✨",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SelforaTextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isTamil) {
                            "ஆரவ் அறையையும் அலமாரியையும் ஆராய்ந்து சுயமாக பயிற்சி செய்ய தயாராகிறான்! வாருங்கள் தொடங்கலாம்!"
                        } else {
                            "Aarav is ready to explore the wardrobe and master step-by-step independence! Let's conquer this session together!"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SelforaTextSecondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Pulsing "Start Practice" CTA Button
                    Button(
                        onClick = onStartPractice,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .scale(buttonPulse),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SelfidoPurple
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isTamil) "பயிற்சியைத் தொடங்கு ➔ 🚀" else "Start Practice Session ➔ 🚀",
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
