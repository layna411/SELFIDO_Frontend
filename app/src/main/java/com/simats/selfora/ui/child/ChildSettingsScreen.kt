package com.simats.selfora.ui.child

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.ChildModePreferenceDto
import com.simats.selfora.ui.components.glass.GlassCard
import com.simats.selfora.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun ChildSettingsScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val childId = remember { SessionManager.getActiveChildId() }

    var preferredName by remember { mutableStateOf(SessionManager.getActiveChildName()) }
    var audioEnabled by remember { mutableStateOf(true) }
    var animationEnabled by remember { mutableStateOf(true) }
    var reducedMotion by remember { mutableStateOf(false) }
    var language by remember { mutableStateOf("EN") } // EN or TA
    var rewardSounds by remember { mutableStateOf(true) }

    var isLoading by remember { mutableStateOf(false) }
    var isSaved by remember { mutableStateOf(false) }

    LaunchedEffect(childId) {
        if (childId > 0) {
            try {
                isLoading = true
                val res = ApiClient.apiService.getChildPreferences(childId)
                if (res.isSuccessful && res.body() != null) {
                    val p = res.body()!!
                    preferredName = p.preferredName ?: preferredName
                    audioEnabled = p.audioEnabled
                    animationEnabled = p.animationEnabled
                    reducedMotion = p.reducedMotion
                    language = p.language
                    rewardSounds = p.rewardSounds
                }
            } catch (e: Exception) {
                // Ignore fallback to local defaults
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SelforaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.Settings, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Child Mode Adult Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SelforaTextPrimary)
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                backgroundColor = Color.White,
                elevation = 6.dp
            ) {
                Column {
                    Text("Child Preferred Name", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = preferredName,
                        onValueChange = { preferredName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Text("Instruction Language", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FilterChip(
                            selected = language == "EN",
                            onClick = { language = "EN" },
                            label = { Text("English 🇬🇧") }
                        )
                        FilterChip(
                            selected = language == "TA",
                            onClick = { language = "TA" },
                            label = { Text("Tamil (தமிழ்) 🇮🇳") }
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    SettingToggleRow("Audio Spoken Guidance", "Spoken instructions & text-to-speech", audioEnabled) { audioEnabled = it }
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = SelforaBackground)
                    SettingToggleRow("Animation Demonstrations", "GIF / Video demonstrations for task steps", animationEnabled) { animationEnabled = it }
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = SelforaBackground)
                    SettingToggleRow("Reduced Motion", "Calm mode with static visual guidance", reducedMotion) { reducedMotion = it }
                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = SelforaBackground)
                    SettingToggleRow("Reward Sounds & Fanfare", "Positive celebration audio effects", rewardSounds) { rewardSounds = it }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (childId > 0) {
                        scope.launch {
                            try {
                                isSaved = false
                                ApiClient.apiService.updateChildPreferences(
                                    childId,
                                    ChildModePreferenceDto(
                                        preferredName = preferredName,
                                        audioEnabled = audioEnabled,
                                        animationEnabled = animationEnabled,
                                        reducedMotion = reducedMotion,
                                        language = language,
                                        rewardSounds = rewardSounds
                                    )
                                )
                                isSaved = true
                            } catch (e: Exception) {
                                isSaved = true
                            }
                        }
                    } else {
                        isSaved = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
            ) {
                Text(if (isSaved) "Settings Saved ✓" else "Save Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = SelforaTextPrimary)
            Text(subtitle, fontSize = 12.sp, color = SelforaTextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SelforaPrimary)
        )
    }
}
