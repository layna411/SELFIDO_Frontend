package com.simats.selfora.ui.therapist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.model.CredentialsSuccessResponse
import com.simats.selfora.ui.components.glass.GlassButton
import com.simats.selfora.ui.components.glass.GlassCard
import com.simats.selfora.ui.components.glass.GlassOutlinedButton
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsSuccessScreen(
    credentials: CredentialsSuccessResponse,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = SelforaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = CircleShape,
                color = SelforaPrimary.copy(alpha = 0.10f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "🎉",
                        fontSize = 32.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Child & Caregiver Created",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Caregiver account has been generated successfully.",
                fontSize = 14.sp,
                color = SelforaTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 6.dp,
                backgroundColor = Color.White
            ) {
                // Child Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Child Name",
                            fontSize = 12.sp,
                            color = SelforaTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = credentials.childName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                    }
                    val isGirl = credentials.gender?.uppercase()?.let { it.startsWith("G") || it.startsWith("F") } ?: false
                    Surface(
                        shape = CircleShape,
                        color = if (isGirl) SelforaSecondary.copy(alpha = 0.15f) else SelforaBlueLight.copy(alpha = 0.5f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(if (isGirl) "👧" else "👦", fontSize = 22.sp)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SelforaBorder.copy(alpha = 0.7f))

                // Caregiver Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Caregiver Name",
                            fontSize = 12.sp,
                            color = SelforaTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = credentials.caregiverName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = SelforaSecondary.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("👩", fontSize = 22.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Relationship:", fontSize = 13.sp, color = SelforaTextSecondary)
                    Text(
                        text = credentials.relationship,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaPrimary
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = SelforaBorder.copy(alpha = 0.7f))

                // Login Info Section
                Text(
                    text = "Caregiver Login Email",
                    fontSize = 12.sp,
                    color = SelforaTextSecondary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = credentials.caregiverEmail,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelforaTextPrimary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Modern Clinical Security Vault Credential Card (High Contrast & Theme-Aligned)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF0F4FF), // Gentle pastel lavender/indigo tint
                    border = BorderStroke(1.5.dp, SelforaPrimary.copy(alpha = 0.4f)),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Password", credentials.temporaryPassword)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Password copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = SelforaPrimary.copy(alpha = 0.15f),
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = SelforaPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "TEMPORARY PASSWORD",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SelforaPrimary,
                                    letterSpacing = 1.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, SelforaPrimary.copy(alpha = 0.35f)),
                                modifier = Modifier.clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Password", credentials.temporaryPassword)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Password copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = SelforaPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Copy",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SelforaPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // High contrast monospace password
                        Text(
                            text = credentials.temporaryPassword,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = Color(0xFF1E1B4B), // Deep Slate Navy for crisp legibility
                            letterSpacing = 2.sp,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Tap box to copy to clipboard",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = SelforaPrimary.copy(alpha = 0.75f),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SelforaWarning.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        tint = SelforaWarning,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "This temporary password must be changed during the caregiver's first login.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SelforaTextPrimary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GlassOutlinedButton(
                    text = "Copy",
                    icon = Icons.Default.ContentCopy,
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(
                            "SELFIDO Credentials",
                            "SELFIDO Caregiver Credentials:\nChild: ${credentials.childName}\nEmail: ${credentials.caregiverEmail}\nTemp Password: ${credentials.temporaryPassword}"
                        )
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Credentials copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f)
                )

                GlassOutlinedButton(
                    text = "Share",
                    icon = Icons.Default.Share,
                    onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Welcome to SELFIDO!\nChild: ${credentials.childName}\nCaregiver Login: ${credentials.caregiverEmail}\nTemporary Password: ${credentials.temporaryPassword}\n\nPlease change your password on first login.")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Credentials"))
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            GlassButton(
                text = "Done",
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
