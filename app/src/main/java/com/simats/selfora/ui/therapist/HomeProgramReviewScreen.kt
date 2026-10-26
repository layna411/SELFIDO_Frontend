package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.components.glass.*
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeProgramReviewScreen(
    childId: Long = 1L,
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            GlassTopBar(
                title = "Home Program Summary (Therapist)",
                subtitle = "Caregiver Home Data Review",
                onBackClick = onBack,
                accentColor = SelforaPrimary
            )
        },
        containerColor = SelforaBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Program Overview Card (Image 1 Screen 13)
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Rohan S. - Home Program", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        Text("12 May 2025", fontSize = 12.sp, color = SelforaTextSecondary)
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = SelforaPrimary.copy(alpha = 0.15f)) {
                        Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = SelforaBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(14.dp))

                ReviewFieldRow(label = "Activity", value = "Tooth Brushing")
                Spacer(modifier = Modifier.height(8.dp))
                ReviewFieldRow(label = "Focus Step", value = "Brush all around teeth")
                Spacer(modifier = Modifier.height(8.dp))
                ReviewFieldRow(label = "Caregiver Instruction", value = "Verbal prompt • Practice 1 time daily")
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Caregiver Response (Next Day) Card (Image 1 Screen 13)
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = Color.White.copy(alpha = 0.95f),
                borderColor = SelforaSuccess.copy(alpha = 0.3f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Feedback, contentDescription = null, tint = SelforaSuccess, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Caregiver Response (Next Day)", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SelforaSuccess.copy(alpha = 0.08f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Completed", fontSize = 13.sp, color = SelforaTextSecondary)
                            Text("Yes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaSuccess)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Prompt Used", fontSize = 13.sp, color = SelforaTextSecondary)
                            Text("Verbal Prompt", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Steps Completed", fontSize = 13.sp, color = SelforaTextSecondary)
                            Text("5 / 7", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("Caregiver Comment:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SelforaBgLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"Needed reminder for applying toothpaste.\"",
                        fontSize = 13.sp,
                        color = SelforaTextSecondary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Action Button (Image 1 Screen 13)
            GlassButton(
                text = "Review & Plan Next Session",
                onClick = onBack,
                icon = Icons.Default.CalendarMonth,
                gradient = listOf(SelforaPrimary, Color(0xFF6D28D9)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            )

            Spacer(modifier = Modifier.height(110.dp))
        }
    }
}

@Composable
fun ReviewFieldRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 11.sp, color = SelforaTextSecondary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
    }
}
