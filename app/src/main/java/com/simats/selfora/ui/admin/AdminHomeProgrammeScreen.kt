package com.simats.selfora.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AdminHomeProgramItem(
    val id: Long,
    val childName: String,
    val therapistName: String,
    val caregiverName: String,
    val activityTitle: String,
    val targetLevel: String,
    val frequency: String,
    val completedCount: Int,
    val totalCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeProgrammeScreen(
    onBack: () -> Unit
) {
    val pinkAccent = Color(0xFFEC4899)

    val programs = remember {
        listOf(
            AdminHomeProgramItem(1L, "Aarav Sharma", "Dr. Sarah Jenkins (OT)", "Priya Sharma", "Dressing - Boy T-Shirt Practice", "INDEPENDENT (L0)", "Daily 1x", 14, 14),
            AdminHomeProgramItem(2L, "Ananya Roy", "Dr. Rajesh Kumar (OT)", "Meera Roy", "Eating - Spoon Feeding Routine", "VERBAL (L3)", "Daily 2x", 10, 14),
            AdminHomeProgramItem(3L, "Kavya Patel", "Dr. Sarah Jenkins (OT)", "Rohan Patel", "Grooming - Hair Brushing Sequence", "GESTURE (L2)", "Daily 1x", 12, 14)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home Programmes Oversight", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(programs) { prog ->
                Surface(
                    modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(prog.activityTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                            Text("${prog.completedCount}/${prog.totalCount} Completed", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = pinkAccent)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Child: ${prog.childName}", fontSize = 12.sp, color = Color(0xFF475569))
                        Text("Therapist: ${prog.therapistName}", fontSize = 12.sp, color = Color(0xFF475569))
                        Text("Caregiver: ${prog.caregiverName}", fontSize = 12.sp, color = Color(0xFF475569))
                        Text("Frequency: ${prog.frequency} • Target: ${prog.targetLevel}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = pinkAccent)
                    }
                }
            }
        }
    }
}
