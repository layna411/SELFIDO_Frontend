package com.simats.selfora.ui.caregiver

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverProfileScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedChildName by remember { mutableStateOf("Rohan S.") }

    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            GlassTopBar(
                title = "Profile",
                subtitle = "Caregiver & Family Account",
                onBackClick = onBack,
                accentColor = SelforaPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Header Card (Image 2 Screen 12)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = SelforaPrimary.copy(alpha = 0.12f),
                        modifier = Modifier.size(60.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👩", fontSize = 32.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text("Meena", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        Text("Caregiver", fontSize = 13.sp, color = SelforaTextSecondary, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // My Children Card & Child Switch (Image 2 Screen 12)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text("My Children", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ChildSelectRow(
                        name = "Rohan S.",
                        ageAndDiagnosis = "Age 7 • ASD",
                        isSelected = selectedChildName == "Rohan S.",
                        onClick = { selectedChildName = "Rohan S." }
                    )

                    ChildSelectRow(
                        name = "Aarav M.",
                        ageAndDiagnosis = "Age 6 • ASD",
                        isSelected = selectedChildName == "Aarav M.",
                        onClick = { selectedChildName = "Aarav M." }
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { Toast.makeText(context, "Add another child", Toast.LENGTH_SHORT).show() }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Add Another Child", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    }
                }
            }

            // Settings & Actions Menu Card (Image 2 Screen 12)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    ProfileMenuOptionRow(
                        icon = Icons.Default.Settings,
                        label = "Account Settings",
                        onClick = {}
                    )
                    Divider(color = SelforaBorder.copy(alpha = 0.4f))

                    ProfileMenuOptionRow(
                        icon = Icons.Default.Notifications,
                        label = "Notification Settings",
                        onClick = {}
                    )
                    Divider(color = SelforaBorder.copy(alpha = 0.4f))

                    ProfileMenuOptionRow(
                        icon = Icons.Default.HelpOutline,
                        label = "Help & Support",
                        onClick = {}
                    )
                    Divider(color = SelforaBorder.copy(alpha = 0.4f))

                    ProfileMenuOptionRow(
                        icon = Icons.Default.ExitToApp,
                        label = "Logout",
                        labelColor = SelforaError,
                        onClick = onLogout
                    )
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
fun ChildSelectRow(
    name: String,
    ageAndDiagnosis: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) SelforaPrimary.copy(alpha = 0.08f) else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) SelforaPrimary else SelforaBorder.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = SelforaBgLight,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("👦", fontSize = 18.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    Text(ageAndDiagnosis, fontSize = 12.sp, color = SelforaTextSecondary)
                }
            }

            if (isSelected) {
                Surface(
                    shape = CircleShape,
                    color = SelforaSuccess,
                    modifier = Modifier.size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenuOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    labelColor: Color = SelforaTextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = labelColor, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = labelColor)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SelforaTextSecondary, modifier = Modifier.size(18.dp))
    }
}
