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
import com.simats.selfora.ui.components.glass.GlassTextField

data class CaregiverItem(
    val id: Long,
    val fullName: String,
    val email: String,
    val phone: String,
    val linkedChildName: String,
    val practiceSessionsCount: Int,
    var isActive: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverManagementScreen(
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    var caregivers by remember {
        mutableStateOf(
            listOf(
                CaregiverItem(1L, "Priya Sharma (Parent)", "priya.sharma@example.com", "+91 9876543211", "Aarav Sharma", 18, true),
                CaregiverItem(2L, "Meera Roy (Parent)", "meera.roy@example.com", "+91 9876543215", "Ananya Roy", 14, true),
                CaregiverItem(3L, "Rohan Patel (Parent)", "rohan.patel@example.com", "+91 9876543219", "Kavya Patel", 11, true),
                CaregiverItem(4L, "Sunita Verma (Parent)", "sunita.verma@example.com", "+91 9876543223", "Rohan Verma", 6, false)
            )
        )
    }

    val purpleAccent = Color(0xFF7C3AED)
    val filteredList = caregivers.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true) ||
                it.linkedChildName.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Caregiver & Parent Accounts", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(16.dp)
        ) {
            GlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Caregivers",
                placeholder = "Search by name, email, or child...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredList) { caregiver ->
                    Surface(
                        modifier = Modifier.fillMaxWidth().shadow(4.dp, RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(48.dp).clip(CircleShape).background(purpleAccent.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = purpleAccent, modifier = Modifier.size(24.dp))
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(caregiver.fullName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Text("Email: ${caregiver.email}", fontSize = 11.sp, color = Color(0xFF64748B))
                                Text("Linked Child: ${caregiver.linkedChildName}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = purpleAccent)
                            }

                            Switch(
                                checked = caregiver.isActive,
                                onCheckedChange = { newStatus ->
                                    caregivers = caregivers.map { if (it.id == caregiver.id) it.copy(isActive = newStatus) else it }
                                    Toast.makeText(context, "Caregiver account updated", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.scale(0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}
