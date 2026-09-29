package com.simats.selfora.ui.therapist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistDashboardScreen(
    onNavigateToChildren: () -> Unit,
    onNavigateToAssessment: () -> Unit,
    onNavigateToSession: () -> Unit,
    onNavigateToHomePrograms: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToMessages: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Therapist Dashboard", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SelforaTextPrimary)
                        Text("Dr. Sarah Jenkins (Occupational Therapist)", fontSize = 12.sp, color = SelforaTextSecondary)
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout", tint = SelforaTextPrimary)
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
                .background(SelforaBgLight)
                .padding(16.dp)
        ) {
            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard("Children", "12", Icons.Default.ChildCare, SelforaBluePrimary, modifier = Modifier.weight(1f))
                MetricCard("Today's Sessions", "5", Icons.Default.Event, SelforaTeal, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard("Pending Reviews", "3", Icons.Default.AssignmentLate, SelforaOrangeWarning, modifier = Modifier.weight(1f))
                MetricCard("Home Programs", "8", Icons.Default.HomeWork, SelforaPurpleAccent, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text("Quick Actions & Workflows", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SelforaTextDark)
            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item { QuickActionCard("My Children", "View & create child profiles", Icons.Default.People, SelforaBluePrimary, onNavigateToChildren) }
                item { QuickActionCard("Assessment", "Baseline ADL task evaluation", Icons.Default.Assessment, SelforaTeal, onNavigateToAssessment) }
                item { QuickActionCard("Therapy Session", "Conduct step-by-step session", Icons.Default.PlayCircle, SelforaGreenSuccess, onNavigateToSession) }
                item { QuickActionCard("Home Programs", "Assign practice to caregivers", Icons.Default.Home, SelforaPurpleAccent, onNavigateToHomePrograms) }
                item { QuickActionCard("Progress & Reports", "Analytics & clinical PDF generator", Icons.Default.BarChart, SelforaOrangeWarning, onNavigateToReports) }
                item { QuickActionCard("Messages", "Caregiver communication thread", Icons.Default.Message, SelforaRedAccent, onNavigateToMessages) }
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = SelforaTextDark)
                Text(title, fontSize = 11.sp, color = SelforaTextMuted)
            }
        }
    }
}

@Composable
fun QuickActionCard(title: String, subtitle: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SelforaTextDark)
            Text(subtitle, fontSize = 11.sp, color = SelforaTextMuted)
        }
    }
}
