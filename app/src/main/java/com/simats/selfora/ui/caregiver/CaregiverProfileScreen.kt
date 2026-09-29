package com.simats.selfora.ui.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverProfileScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var soundEnabled by remember { mutableStateOf(true) }
    var ttsEnabled by remember { mutableStateOf(true) }
    var autoPlayGuidance by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings", fontWeight = FontWeight.Bold, color = SelforaTextPrimary) },
                modifier = Modifier.statusBarsPadding(),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = SelforaTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SelforaSurface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBackground)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SelforaSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SelforaPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(40.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Sunita Sharma", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SelforaTextPrimary)
                    Text("Caregiver / Parent", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SelforaPrimary)
                    Text("sunita.sharma@example.com • +91 98765 43210", fontSize = 11.sp, color = SelforaTextSecondary)

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SelforaBackground,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("LINKED CHILDREN:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("👦 Aarav Sharma (Boy, 6 yrs)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                            Text("👧 Ananya Sharma (Girl, 5 yrs)", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SelforaTextPrimary)
                        }
                    }
                }
            }

            // Settings Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SelforaSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Practice Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingSwitchRow("Sound Effects", "Play audio cues during ADL practice", soundEnabled) { soundEnabled = it }
                    Divider(color = SelforaBorder, modifier = Modifier.padding(vertical = 8.dp))

                    SettingSwitchRow("Text-to-Speech Guidance", "Read step guidance aloud for child", ttsEnabled) { ttsEnabled = it }
                    Divider(color = SelforaBorder, modifier = Modifier.padding(vertical = 8.dp))

                    SettingSwitchRow("Auto-play Guidance", "Automatically play spoken audio on step change", autoPlayGuidance) { autoPlayGuidance = it }
                    Divider(color = SelforaBorder, modifier = Modifier.padding(vertical = 8.dp))

                    SettingSwitchRow("Notifications", "Receive reminders and therapist updates", notificationsEnabled) { notificationsEnabled = it }
                }
            }

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout Caregiver Account", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun SettingSwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaTextPrimary)
            Text(subtitle, fontSize = 11.sp, color = SelforaTextSecondary)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SelforaPrimary)
        )
    }
}
