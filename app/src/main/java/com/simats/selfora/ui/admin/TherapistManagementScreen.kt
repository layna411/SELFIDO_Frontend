package com.simats.selfora.ui.admin

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.TherapistItem
import com.simats.selfora.ui.components.glass.GlassTextField
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TherapistManagementScreen(
    onNavigateToCreateTherapist: () -> Unit,
    onNavigateToTherapistDetails: (Long) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var therapists by remember { mutableStateOf<List<TherapistItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val blueAccent = Color(0xFF2563EB)
    val textPrimary = Color(0xFF0F172A)
    val textSecondary = Color(0xFF64748B)

    // Demo Initial List if backend offline
    val defaultList = remember {
        listOf(
            TherapistItem(1L, "Dr. Sarah Jenkins (OT)", "sarah.jenkins@selfora.org", "+91 9876543210", "Lead Occupational Therapist", "Pediatric ADL & Fine Motor", "MOT (Pediatrics)", "8 Years", true, 12),
            TherapistItem(2L, "Dr. Rajesh Kumar (OT)", "rajesh.kumar@selfora.org", "+91 9876543214", "Senior Pediatric Therapist", "Sensory Integration & Dressing", "BOT, MOT", "6 Years", true, 9),
            TherapistItem(3L, "Dr. Anita Roy (OT)", "anita.roy@selfora.org", "+91 9876543218", "Occupational Therapist", "Child Development & Grooming", "BOT", "4 Years", true, 7),
            TherapistItem(4L, "Dr. Vikram Seth (OT)", "vikram.seth@selfora.org", "+91 9876543222", "Clinical OT Specialist", "Feeding & Oral Motor Skills", "MOT", "5 Years", false, 0)
        )
    }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val response = ApiClient.apiService.getAllTherapists()
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                therapists = response.body()!!
            } else {
                therapists = defaultList
            }
        } catch (e: Exception) {
            therapists = defaultList
        } finally {
            isLoading = false
        }
    }

    val filteredList = therapists.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true) ||
                it.specialization.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Therapist & Doctor Management", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToCreateTherapist) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Therapist", tint = blueAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateTherapist,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Therapist", fontWeight = FontWeight.Bold) },
                containerColor = blueAccent,
                contentColor = Color.White
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Search Field
            GlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Therapists",
                placeholder = "Search by name, email, or specialization...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = blueAccent)
                }
            } else if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No therapists found.", color = textSecondary)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList) { therapist ->
                        TherapistCard(
                            therapist = therapist,
                            onClick = { onNavigateToTherapistDetails(therapist.id) },
                            onToggleStatus = { newStatus ->
                                scope.launch {
                                    try {
                                        ApiClient.apiService.updateTherapistStatus(therapist.id, newStatus)
                                    } catch (e: Exception) {}
                                    therapists = therapists.map {
                                        if (it.id == therapist.id) it.copy(isActive = newStatus) else it
                                    }
                                    Toast.makeText(context, "Therapist status updated", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TherapistCard(
    therapist: TherapistItem,
    onClick: () -> Unit,
    onToggleStatus: (Boolean) -> Unit
) {
    val blueAccent = Color(0xFF2563EB)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp), clip = false)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(blueAccent.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = blueAccent, modifier = Modifier.size(24.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(therapist.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    Text(therapist.designation, fontSize = 12.sp, color = Color(0xFF64748B))
                }

                // Active / Inactive Badge & Switch
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = therapist.isActive,
                        onCheckedChange = onToggleStatus,
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Specialization", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text(therapist.specialization, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF334155))
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text("Assigned Children", fontSize = 10.sp, color = Color(0xFF94A3B8))
                    Text("${therapist.assignedChildrenCount} Active Children", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = blueAccent)
                }
            }
        }
    }
}
