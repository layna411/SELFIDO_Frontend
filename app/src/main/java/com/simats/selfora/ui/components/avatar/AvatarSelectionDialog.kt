package com.simats.selfora.ui.components.avatar

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.theme.*

@Composable
fun AvatarSelectionDialog(
    initialSelected: ChildAvatarType = ChildAvatarType.BOY,
    onDismiss: () -> Unit,
    onAvatarSelected: (ChildAvatarType) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialSelected) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Select Avatar",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelforaTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose your preferred 3D character avatar",
                    fontSize = 12.sp,
                    color = SelforaTextSecondary
                )
            }
        },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Boy Choice
                val isBoySelected = selectedType == ChildAvatarType.BOY
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedType = ChildAvatarType.BOY }
                        .then(
                            if (isBoySelected) Modifier.border(2.5.dp, SelforaPrimary, RoundedCornerShape(16.dp))
                            else Modifier
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBoySelected) SelforaPrimary.copy(alpha = 0.1f) else SelforaSurface
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        ChildAvatar(
                            avatarType = ChildAvatarType.BOY,
                            avatarSize = 72.dp,
                            avatarPose = AvatarPose.STANDARD,
                            backgroundStyle = AvatarBackgroundStyle.NONE
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Boy Avatar", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaTextPrimary)
                        Text("Aarav • 7 yrs", fontSize = 11.sp, color = SelforaPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        RadioButton(
                            selected = isBoySelected,
                            onClick = { selectedType = ChildAvatarType.BOY },
                            colors = RadioButtonDefaults.colors(selectedColor = SelforaPrimary)
                        )
                    }
                }

                // Girl Choice
                val isGirlSelected = selectedType == ChildAvatarType.GIRL
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedType = ChildAvatarType.GIRL }
                        .then(
                            if (isGirlSelected) Modifier.border(2.5.dp, Color(0xFF9333EA), RoundedCornerShape(16.dp))
                            else Modifier
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isGirlSelected) Color(0xFF9333EA).copy(alpha = 0.1f) else SelforaSurface
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        ChildAvatar(
                            avatarType = ChildAvatarType.GIRL,
                            avatarSize = 72.dp,
                            avatarPose = AvatarPose.STANDARD,
                            backgroundStyle = AvatarBackgroundStyle.NONE
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Girl Avatar", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaTextPrimary)
                        Text("Ananya • 6 yrs", fontSize = 11.sp, color = Color(0xFF9333EA))
                        Spacer(modifier = Modifier.height(8.dp))
                        RadioButton(
                            selected = isGirlSelected,
                            onClick = { selectedType = ChildAvatarType.GIRL },
                            colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF9333EA))
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAvatarSelected(selectedType)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedType == ChildAvatarType.GIRL) Color(0xFF9333EA) else SelforaPrimary
                )
            ) {
                Text("Use Selected Avatar", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
            }
        },
        containerColor = SelforaSurface,
        shape = RoundedCornerShape(24.dp)
    )
}
