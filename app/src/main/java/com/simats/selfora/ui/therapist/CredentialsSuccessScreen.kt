package com.simats.selfora.ui.therapist

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

    Scaffold(
        containerColor = SelforaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎉",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

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

            Spacer(modifier = Modifier.height(24.dp))

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 12.dp,
                backgroundColor = Color.White.copy(alpha = 0.92f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Child Name", fontSize = 12.sp, color = SelforaTextSecondary)
                        Text(credentials.childName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    }
                    Text("👦", fontSize = 28.sp)
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = SelforaBorder.copy(alpha = 0.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Caregiver Name", fontSize = 12.sp, color = SelforaTextSecondary)
                        Text(credentials.caregiverName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    }
                    Text("👩", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Relationship:", fontSize = 13.sp, color = SelforaTextSecondary)
                    Text(credentials.relationship, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = SelforaPrimary)
                }

                Divider(modifier = Modifier.padding(vertical = 12.dp), color = SelforaBorder.copy(alpha = 0.5f))

                Text("Caregiver Login Email", fontSize = 12.sp, color = SelforaTextSecondary)
                Text(credentials.caregiverEmail, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, shape = RoundedCornerShape(16.dp))
                        .background(SelforaBlueLight.copy(alpha = 0.7f))
                        .border(1.2.dp, SelforaPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TEMPORARY PASSWORD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = credentials.temporaryPassword,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SelforaTextPrimary,
                            letterSpacing = 2.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SelforaWarning.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = SelforaWarning, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "This temporary password must be changed during the caregiver's first login.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = SelforaTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

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
                            "SELFIDO Caregiver Credentials:\nEmail: ${credentials.caregiverEmail}\nTemp Password: ${credentials.temporaryPassword}"
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

            Spacer(modifier = Modifier.height(14.dp))

            GlassButton(
                text = "Done",
                onClick = onDone,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
