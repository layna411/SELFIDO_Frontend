package com.simats.selfora.ui.child

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.RecordStepProgressRequest
import com.simats.selfora.ui.components.avatar.AvatarBackgroundStyle
import com.simats.selfora.ui.components.avatar.AvatarPose
import com.simats.selfora.ui.components.avatar.ChildAvatar
import com.simats.selfora.ui.components.avatar.ChildAvatarType
import com.simats.selfora.ui.components.glass.GlassCard
import com.simats.selfora.ui.theme.*
import com.simats.selfora.utils.TtsManager
import kotlinx.coroutines.launch

@Composable
fun ChildActivityCompletionScreen(
    activityId: Long,
    activityTitle: String = "ADL Practice",
    onReplay: () -> Unit,
    onGoHome: () -> Unit
) {
    val context = LocalContext.current
    val ttsManager = remember { TtsManager(context) }
    val scope = rememberCoroutineScope()

    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(activityId) {
        ttsManager.speak("Awesome job! You completed $activityTitle! You earned 5 stars!")
        
        val childId = SessionManager.getActiveChildId()
        if (childId > 0) {
            scope.launch {
                try {
                    isSubmitting = true
                    ApiClient.apiService.recordChildStepProgress(
                        RecordStepProgressRequest(
                            childId = childId,
                            activityId = activityId,
                            isActivityCompleted = true
                        )
                    )
                } catch (e: Exception) {
                    // Ignore network failure on local child completion display
                } finally {
                    isSubmitting = false
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    // Celebration pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "star_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChildRewardsCardBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // 3D Avatar Celebration Pose (Matching Screen 8 Reference Design)
            val isGirl = SessionManager.getActiveChildGender().equals("GIRL", ignoreCase = true)
            ChildAvatar(
                avatarType = if (isGirl) ChildAvatarType.GIRL else ChildAvatarType.BOY,
                avatarSize = 160.dp,
                avatarPose = AvatarPose.CELEBRATION,
                backgroundStyle = AvatarBackgroundStyle.NONE
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Great Job! 🎉",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SelforaTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "You completed $activityTitle!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Earned Rewards Glass Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                backgroundColor = Color.White.copy(alpha = 0.95f),
                elevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star",
                            tint = ChildYellowStar,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "+5 Stars Earned! 🌟",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = SelforaSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Action Buttons
            Button(
                onClick = onReplay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = SelforaPrimary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Practise Again 🔄", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ChildGreenPlay)
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back to Fun Home 🏠", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
            }
        }
    }
}
