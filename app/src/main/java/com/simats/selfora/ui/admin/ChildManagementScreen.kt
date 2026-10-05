package com.simats.selfora.ui.admin

import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.R
import com.simats.selfora.ui.components.glass.GlassTextField

data class AdminChildItem(
    val id: Long,
    val name: String,
    val age: Int,
    val gender: String,
    val assignedTherapist: String,
    val caregiverName: String,
    val activeActivity: String,
    val promptLevel: String,
    val independencePercentage: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildManagementScreen(
    onNavigateToChildDetails: (Long) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val children = remember {
        listOf(
            AdminChildItem(1L, "Aarav Sharma", 6, "MALE", "Dr. Sarah Jenkins (OT)", "Priya Sharma (Parent)", "Dressing - Upper Body", "INDEPENDENT (L0)", 85),
            AdminChildItem(2L, "Ananya Roy", 7, "FEMALE", "Dr. Rajesh Kumar (OT)", "Meera Roy (Parent)", "Eating & Utensil Use", "VERBAL (L3)", 68),
            AdminChildItem(3L, "Kavya Patel", 6, "FEMALE", "Dr. Sarah Jenkins (OT)", "Rohan Patel (Parent)", "Grooming & Hygiene", "GESTURE (L2)", 74),
            AdminChildItem(4L, "Rohan Verma", 8, "MALE", "Dr. Anita Roy (OT)", "Sunita Verma (Parent)", "Shoes & Socks Fastening", "MODEL (L4)", 52)
        )
    }

    val filteredList = children.filter {
        it.name.contains(searchQuery, ignoreCase = true) ||
                it.assignedTherapist.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Centralized Child Management", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp)
        ) {
            GlassTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = "Search Children",
                placeholder = "Search by name or therapist...",
                leadingIcon = Icons.Default.Search,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredList) { child ->
                    AdminChildCard(
                        child = child,
                        onClick = { onNavigateToChildDetails(child.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminChildCard(
    child: AdminChildItem,
    onClick: () -> Unit
) {
    val isBoy = child.gender == "MALE"
    val avatarDrawable = if (isBoy) R.drawable.boy_avatar else R.drawable.girl_avatar
    val accentColor = if (isBoy) Color(0xFF2563EB) else Color(0xFF7C3AED)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = avatarDrawable),
                contentDescription = child.name,
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(child.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                Text("Age ${child.age} • ${child.gender}", fontSize = 11.sp, color = Color(0xFF64748B))

                Spacer(modifier = Modifier.height(4.dp))

                Text("Therapist: ${child.assignedTherapist}", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = accentColor)
                Text("Caregiver: ${child.caregiverName}", fontSize = 11.sp, color = Color(0xFF64748B))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("${child.independencePercentage}%", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = accentColor)
                Text("Independence", fontSize = 10.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}
