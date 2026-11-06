package com.simats.selfora.ui.caregiver

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.local.SessionManager
import com.simats.selfora.data.model.ChildDto
import com.simats.selfora.ui.components.glass.GlassCard
import com.simats.selfora.ui.components.glass.GlassTopBar
import com.simats.selfora.ui.theme.*
import com.simats.selfora.utils.LanguageManager
import kotlinx.coroutines.launch

data class LinkedChildProfile(
    val id: Long,
    val name: String,
    val details: String,
    val gender: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverProfileScreen(
    onLogout: () -> Unit,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isTamil = currentLanguage == "ta"

    // Dynamic Caregiver & Active Child Data
    var caregiverName by remember {
        mutableStateOf(SessionManager.getUsername().ifBlank { "Meena (Caregiver)" })
    }
    var activeChildId by remember {
        mutableStateOf(SessionManager.getActiveChildId())
    }
    var activeChildName by remember {
        mutableStateOf(SessionManager.getActiveChildName())
    }

    var childrenList by remember {
        mutableStateOf(
            listOf(
                LinkedChildProfile(
                    id = activeChildId,
                    name = activeChildName,
                    details = "Age 6 • Sensory & ADL Dressing Plan",
                    gender = SessionManager.getActiveChildGender()
                ),
                LinkedChildProfile(
                    id = if (activeChildId == 1L) 2L else 1L,
                    name = if (activeChildId == 1L) "Rohan Sharma" else "Aarav Sharma",
                    details = "Age 7 • Fine Motor & Routine Self-Care",
                    gender = "BOY"
                )
            )
        )
    }

    // Dialog Visibility States
    var showAddChildDialog by remember { mutableStateOf(false) }
    var showAccountSettingsDialog by remember { mutableStateOf(false) }
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    // Notification Preferences
    var practiceRemindersEnabled by remember { mutableStateOf(true) }
    var therapistFeedbackEnabled by remember { mutableStateOf(true) }
    var assessmentAlertsEnabled by remember { mutableStateOf(true) }
    var spokenVoiceEnabled by remember { mutableStateOf(true) }

    // Fetch live linked children from backend API if available
    LaunchedEffect(Unit) {
        try {
            val res = ApiClient.apiService.getMyChildren()
            if (res.isSuccessful && !res.body().isNullOrEmpty()) {
                val apiChildren = res.body()!!.map { c ->
                    val fullName = "${c.firstName} ${c.lastName}".trim()
                    LinkedChildProfile(
                        id = c.id ?: 1L,
                        name = fullName.ifBlank { "Child #${c.id}" },
                        details = "DOB: ${c.dateOfBirth} • ${c.gender} • ${c.diagnosisNotes ?: "ADL Plan"}",
                        gender = c.gender
                    )
                }
                childrenList = apiChildren
            }
        } catch (_: Exception) {}
    }

    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            GlassTopBar(
                title = if (isTamil) "சுயவிவரம்" else "Profile",
                subtitle = if (isTamil) "பெற்றோர் & குடும்ப கணக்கு" else "Caregiver & Family Account",
                accentColor = SelforaPrimary,
                showLanguageToggle = true
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Caregiver Header Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.5.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SelforaPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.5.dp, SelforaPrimary.copy(alpha = 0.25f)),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = caregiverName.take(2).uppercase(),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SelforaPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = caregiverName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = SelforaPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isTamil) "முதன்மை பெற்றோர் / பராமரிப்பாளர்" else "Primary Caregiver / Parent Account",
                            fontSize = 11.5.sp,
                            color = SelforaTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isTamil) "செயலில் உள்ள கணக்கு" else "Active Family Profile",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }

            // Linked Children Card & Child Switching
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.5.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isTamil) "இணைக்கப்பட்ட குழந்தைகள்" else "Linked Children Profiles",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF1F5F9)
                        ) {
                            Text(
                                text = "${childrenList.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        childrenList.forEach { child ->
                            val isSelected = child.id == activeChildId

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) SelforaPrimary.copy(alpha = 0.08f) else Color(0xFFF8FAFC),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) SelforaPrimary else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        activeChildId = child.id
                                        activeChildName = child.name
                                        SessionManager.saveActiveChildContext(child.id, child.name, child.gender)
                                        Toast.makeText(
                                            context,
                                            if (isTamil) "செயலில் உள்ள குழந்தை: ${child.name}" else "Active Child Switched: ${child.name}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) SelforaPrimary else Color(0xFFE2E8F0),
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.ChildCare,
                                                    contentDescription = null,
                                                    tint = if (isSelected) Color.White else SelforaTextSecondary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = child.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SelforaTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = child.details,
                                                fontSize = 11.sp,
                                                color = SelforaTextSecondary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SelforaPrimary
                                        ) {
                                            Text(
                                                text = if (isTamil) "செயலில் உள்ளது ✓" else "Active ✓",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // + Add Another Child Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, SelforaPrimary.copy(alpha = 0.35f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAddChildDialog = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 11.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = SelforaPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isTamil) "+ புதிய குழந்தையைச் சேர்க்க" else "+ Add Another Child Profile",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SelforaPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Settings & Actions Menu Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.5.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    ProfileMenuRow(
                        icon = Icons.Default.AccountCircle,
                        label = if (isTamil) "கணக்கு அமைப்புகள்" else "Account Settings",
                        subtitle = if (isTamil) "பெயர், மின்னஞ்சல், தொலைபேசி விவரங்கள்" else "Manage caregiver name, email & clinic contact",
                        iconTint = SelforaPrimary,
                        onClick = { showAccountSettingsDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFE2E8F0))

                    ProfileMenuRow(
                        icon = Icons.Default.Notifications,
                        label = if (isTamil) "அறிவிப்பு விருப்பங்கள்" else "Notification Settings",
                        subtitle = if (isTamil) "பயிற்சி நினைவூட்டல் & மருத்துவர் செய்திகள்" else "Daily practice alarms & therapist alert preferences",
                        iconTint = Color(0xFFF59E0B),
                        onClick = { showNotificationSettingsDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFE2E8F0))

                    ProfileMenuRow(
                        icon = Icons.AutoMirrored.Filled.HelpOutline,
                        label = if (isTamil) "உதவி & மருத்துவ ஆதரவு" else "Help & Clinical Support",
                        subtitle = if (isTamil) "SIMATS மருத்துவமனை அவசர உதவி எண்" else "Direct clinic hotline, therapist contact & guide",
                        iconTint = Color(0xFF0D9488),
                        onClick = { showHelpDialog = true }
                    )

                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color(0xFFE2E8F0))

                    ProfileMenuRow(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        label = if (isTamil) "வெளியேறு" else "Logout",
                        subtitle = if (isTamil) "பயன்பாட்டிலிருந்து பாதுகாப்பாக வெளியேறுக" else "Safely exit this caregiver session",
                        iconTint = Color(0xFFEF4444),
                        labelColor = Color(0xFFEF4444),
                        onClick = { showLogoutConfirmDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(110.dp))
        }
    }

    // 1. ADD CHILD DIALOG
    if (showAddChildDialog) {
        var newFirstName by remember { mutableStateOf("") }
        var newLastName by remember { mutableStateOf("") }
        var newGender by remember { mutableStateOf("BOY") }
        var newAge by remember { mutableStateOf("6") }

        AlertDialog(
            onDismissRequest = { showAddChildDialog = false },
            title = {
                Text(
                    text = if (isTamil) "புதிய குழந்தையைச் சேர்க்க" else "Add New Child Profile",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SelforaTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newFirstName,
                        onValueChange = { newFirstName = it },
                        label = { Text(if (isTamil) "முதல் பெயர்" else "First Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newLastName,
                        onValueChange = { newLastName = it },
                        label = { Text(if (isTamil) "கடைசி பெயர்" else "Last Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = newAge,
                        onValueChange = { newAge = it },
                        label = { Text(if (isTamil) "வயது (ஆண்டுகள்)" else "Age (Years)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { newGender = "BOY" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (newGender == "BOY") SelforaPrimary else Color(0xFFF1F5F9),
                                contentColor = if (newGender == "BOY") Color.White else SelforaTextPrimary
                            )
                        ) {
                            Text(if (isTamil) "ஆண் குழந்தை" else "Boy")
                        }
                        Button(
                            onClick = { newGender = "GIRL" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (newGender == "GIRL") SelforaSecondary else Color(0xFFF1F5F9),
                                contentColor = if (newGender == "GIRL") Color.White else SelforaTextPrimary
                            )
                        ) {
                            Text(if (isTamil) "பெண் குழந்தை" else "Girl")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newFirstName.isNotBlank()) {
                            val newId = (childrenList.size + 10).toLong()
                            val fullChildName = "${newFirstName.trim()} ${newLastName.trim()}".trim()
                            val newChild = LinkedChildProfile(
                                id = newId,
                                name = fullChildName,
                                details = "Age $newAge • $newGender • ADL Dressing Plan",
                                gender = newGender
                            )
                            childrenList = childrenList + newChild
                            activeChildId = newId
                            activeChildName = fullChildName
                            SessionManager.saveActiveChildContext(newId, fullChildName, newGender)
                            showAddChildDialog = false
                            Toast.makeText(
                                context,
                                if (isTamil) "குழந்தை வெற்றிகரமாக சேர்க்கப்பட்டது!" else "Child profile linked successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                ) {
                    Text(if (isTamil) "சேமி" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddChildDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }

    // 2. ACCOUNT SETTINGS DIALOG
    if (showAccountSettingsDialog) {
        var editName by remember { mutableStateOf(caregiverName) }
        var editPhone by remember { mutableStateOf("+91 98401 23456") }
        var editEmail by remember { mutableStateOf("meena.caregiver@selfora.health") }

        AlertDialog(
            onDismissRequest = { showAccountSettingsDialog = false },
            title = {
                Text(
                    text = if (isTamil) "கணக்கு அமைப்புகள்" else "Account Information",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SelforaTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text(if (isTamil) "பராமரிப்பாளர் பெயர்" else "Caregiver Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text(if (isTamil) "மின்னஞ்சல்" else "Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text(if (isTamil) "தொலைபேசி எண்" else "Mobile Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isTamil) "இணைக்கப்பட்ட மையம்: SIMATS Pediatric Occupational Therapy Center" else "Affiliated: SIMATS Pediatric Occupational Therapy Center",
                            fontSize = 11.sp,
                            color = SelforaTextSecondary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            caregiverName = editName.trim()
                            showAccountSettingsDialog = false
                            Toast.makeText(
                                context,
                                if (isTamil) "சுயவிவர அமைப்புகள் புதுப்பிக்கப்பட்டன!" else "Profile settings updated successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                ) {
                    Text(if (isTamil) "புதுப்பி" else "Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountSettingsDialog = false }) {
                    Text(if (isTamil) "மூடு" else "Close")
                }
            }
        )
    }

    // 3. NOTIFICATION SETTINGS DIALOG
    if (showNotificationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationSettingsDialog = false },
            title = {
                Text(
                    text = if (isTamil) "அறிவிப்பு விருப்பங்கள்" else "Notification Preferences",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SelforaTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTamil) "தினசரி பயிற்சி நினைவூட்டல்" else "Daily Practice Reminders",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextPrimary
                            )
                            Text(
                                text = if (isTamil) "காலை 8:00 மணிக்கு நினைவூட்டும்" else "Alerts at 8:00 AM daily",
                                fontSize = 11.sp,
                                color = SelforaTextSecondary
                            )
                        }
                        Switch(
                            checked = practiceRemindersEnabled,
                            onCheckedChange = { practiceRemindersEnabled = it }
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTamil) "சிகிச்சையாளர் செய்திகள்" else "Therapist Messages & Alerts",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextPrimary
                            )
                            Text(
                                text = if (isTamil) "மருத்துவர் கருத்துகளை உடனுக்குடன் அறிய" else "Live updates from Dr. Priya OT",
                                fontSize = 11.sp,
                                color = SelforaTextSecondary
                            )
                        }
                        Switch(
                            checked = therapistFeedbackEnabled,
                            onCheckedChange = { therapistFeedbackEnabled = it }
                        )
                    }

                    HorizontalDivider(color = Color(0xFFE2E8F0))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTamil) "குரல் வழிகாட்டல் ஒலி" else "Spoken Voice Instructions",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextPrimary
                            )
                            Text(
                                text = if (isTamil) "படிநிலைகளுக்கான தமிழ்/ஆங்கில குரல்" else "Enable audio prompt playback during steps",
                                fontSize = 11.sp,
                                color = SelforaTextSecondary
                            )
                        }
                        Switch(
                            checked = spokenVoiceEnabled,
                            onCheckedChange = { spokenVoiceEnabled = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showNotificationSettingsDialog = false
                        Toast.makeText(
                            context,
                            if (isTamil) "அமைப்புகள் சேமிக்கப்பட்டன!" else "Preferences saved!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary)
                ) {
                    Text(if (isTamil) "முடிந்தது" else "Done")
                }
            }
        )
    }

    // 4. HELP & SUPPORT DIALOG
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Text(
                    text = if (isTamil) "உதவி & மருத்துவ ஆதரவு" else "Clinical Help & Support",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SelforaTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "SIMATS Pediatric OT Center",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "Saveetha Institute of Medical and Technical Sciences, Chennai",
                                fontSize = 11.5.sp,
                                color = Color(0xFF15803D)
                            )
                        }
                    }

                    Text(
                        text = if (isTamil) "தொலைபேசி: +91 44 2680 1580" else "Clinic Phone: +91 44 2680 1580",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SelforaTextPrimary
                    )
                    Text(
                        text = if (isTamil) "மின்னஞ்சல்: support@selfora.health" else "Support Email: support@selfora.health",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = SelforaTextPrimary
                    )
                    Text(
                        text = if (isTamil) "நேரம்: திங்கள் - சனி (காலை 8:30 - மாலை 5:30)" else "Hours: Mon - Sat (8:30 AM - 5:30 PM)",
                        fontSize = 12.sp,
                        color = SelforaTextSecondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+914426801580"))
                        try {
                            context.startActivity(callIntent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "+91 44 2680 1580", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isTamil) "அழைக்க" else "Call Clinic")
                }
            },
            dismissButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(if (isTamil) "மூடு" else "Close")
                }
            }
        )
    }

    // 5. LOGOUT CONFIRMATION DIALOG
    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = {
                Text(
                    text = if (isTamil) "வெளியேற விரும்புகிறீர்களா?" else "Confirm Logout",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.5.sp,
                    color = SelforaTextPrimary
                )
            },
            text = {
                Text(
                    text = if (isTamil) "உங்கள் பராமரிப்பாளர் கணக்கிலிருந்து வெளியேற உறுதிப்படுத்தவும்." else "Are you sure you want to log out from this caregiver account?",
                    fontSize = 13.sp,
                    color = SelforaTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirmDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SelforaError)
                ) {
                    Text(if (isTamil) "வெளியேறு" else "Logout", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text(if (isTamil) "ரத்து" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun ProfileMenuRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    iconTint: Color,
    labelColor: Color = SelforaTextPrimary,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = iconTint.copy(alpha = 0.10f),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = labelColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = SelforaTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = SelforaTextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
