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
    val lastProgress = remember { com.simats.selfora.data.repository.PracticeSessionStore.getLastStepProgress() }

    val displayTitle = lastProgress?.activityTitle?.ifBlank { activityTitle } ?: activityTitle
    val starsAwarded = lastProgress?.starsAwarded ?: 5
    val totalStars = lastProgress?.totalStars ?: 5
    val newBadge = lastProgress?.newlyUnlockedBadge

    LaunchedEffect(activityId) {
        val ttsMessage = if (newBadge != null) {
            "Awesome job! You completed $displayTitle! And you unlocked a new badge: $newBadge!"
        } else if (starsAwarded > 0) {
            "Awesome job! You completed $displayTitle! You earned 5 stars!"
        } else {
            "Awesome job! You completed $displayTitle practice again!"
        }
        ttsManager.speak(ttsMessage)
    }

    DisposableEffect(Unit) {
        onDispose { ttsManager.shutdown() }
    }

    // Celebration pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
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
            // 3D Avatar Celebration Pose
            val isGirl = SessionManager.getActiveChildGender().equals("GIRL", ignoreCase = true)
            ChildAvatar(
                avatarType = if (isGirl) ChildAvatarType.GIRL else ChildAvatarType.BOY,
                avatarSize = 140.dp,
                avatarPose = AvatarPose.CELEBRATION,
                backgroundStyle = AvatarBackgroundStyle.NONE
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Great Job! 🎉",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SelforaTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "You completed $displayTitle!",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Newly Unlocked Badge Celebration Banner
            if (newBadge != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFEF3C7),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFF59E0B)),
                    shadowElevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏆", fontSize = 36.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("NEW BADGE UNLOCKED! ✨", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFFB45309))
                            Text(newBadge, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SelforaTextPrimary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Earned Rewards Child Progress Component
            com.simats.selfora.ui.components.progress.ChildRewardProgress(
                starsEarned = totalStars,
                totalStars = maxOf(10, totalStars),
                currentStreak = 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (starsAwarded > 0) "+5 Super Stars Added to Balance! ⭐" else "Activity Mastered! Replay Practice Complete! 🌟",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (starsAwarded > 0) Color(0xFFD97706) else SelforaTextSecondary
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons
            Button(
                onClick = onReplay,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = SelforaPrimary)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Practise Again 🔄", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onGoHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ChildGreenPlay)
            ) {
                Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back to Fun Home 🏠", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
            }
        }
    }
}
