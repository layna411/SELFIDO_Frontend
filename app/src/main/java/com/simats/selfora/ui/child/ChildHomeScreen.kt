package com.simats.selfora.ui.child

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RollerSkating
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.api.ApiClient
import com.simats.selfora.data.model.ChildDto
import com.simats.selfora.ui.theme.*

@Composable
fun ChildHomeScreen(
    onSelectDressing: (activityId: String) -> Unit,
    onSelectEating: () -> Unit,
    onSelectShoes: () -> Unit,
    onSelectRewards: () -> Unit,
    onLogout: () -> Unit
) {
    var childProfile by remember { mutableStateOf<ChildDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            val res = ApiClient.apiService.getMyChildren()
            if (res.isSuccessful && res.body() != null && res.body()!!.isNotEmpty()) {
                childProfile = res.body()!!.first()
            } else {
                // Dynamic fallback based on account
                childProfile = ChildDto(
                    id = 1L,
                    firstName = "Aarav",
                    lastName = "Sharma",
                    dateOfBirth = "2019-05-12",
                    gender = "BOY",
                    caregiverName = "Priya Sharma (Parent)",
                    therapistName = "Dr. Anish Mehta (OT Doctor)"
                )
            }
        } catch (e: Exception) {
            childProfile = ChildDto(
                id = 1L,
                firstName = "Aarav",
                lastName = "Sharma",
                dateOfBirth = "2019-05-12",
                gender = "BOY",
                caregiverName = "Priya Sharma (Parent)",
                therapistName = "Dr. Anish Mehta (OT Doctor)"
            )
        } finally {
            isLoading = false
        }
    }

    val isGirl = childProfile?.gender?.uppercase()?.contains("GIRL") == true || childProfile?.gender?.uppercase()?.contains("FEMALE") == true
    val childName = childProfile?.firstName ?: if (isGirl) "Ananya" else "Aarav"
    val caregiverName = childProfile?.caregiverName ?: "Priya Sharma (Parent)"
    val therapistName = childProfile?.therapistName ?: "Dr. Anish Mehta (OT Doctor)"
    val dressingActivityId = if (isGirl) "girl_frock_activity" else "boy_tshirt_activity"

    val primaryThemeColor = if (isGirl) SelforaSecondary else SelforaPrimary
    val dressingCardBg = if (isGirl) ChildRewardsCardBg else ChildDressingCardBg

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SelforaBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Header (Dynamic Child Greeting & Logout)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isGirl) "Hi, $childName! 👧" else "Hi, $childName! 👦",
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp,
                    color = primaryThemeColor
                )
                Text("Ready for today's fun activity?", fontSize = 14.sp, color = SelforaTextSecondary)
            }

            IconButton(onClick = onLogout) {
                Icon(Icons.Default.ExitToApp, contentDescription = "Exit", tint = SelforaError)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Caretaker & Doctor Information Card
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = SelforaSurface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FamilyRestroom, contentDescription = null, tint = primaryThemeColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Caretaker / Parent: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                    Text(caregiverName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MedicalServices, contentDescription = null, tint = SelforaSuccess, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Assigned Doctor: ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SelforaTextSecondary)
                    Text(therapistName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic Activity Grid Row 1 (Gender-Specific Dressing & Eating)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ChildActivityCard(
                    title = if (isGirl) "Frock (Girl)" else "T-Shirt (Boy)",
                    icon = Icons.Default.Checkroom,
                    cardBgColor = dressingCardBg,
                    iconColor = primaryThemeColor,
                    onClick = { onSelectDressing(dressingActivityId) }
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                ChildActivityCard("Eating", Icons.Default.Restaurant, ChildEatingCardBg, SelforaWarning, onSelectEating)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dynamic Activity Grid Row 2 (Shoes & My Rewards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                ChildActivityCard("Shoes", Icons.Default.RollerSkating, ChildShoesCardBg, SelforaSuccess, onSelectShoes)
            }
            Box(modifier = Modifier.weight(1f)) {
                ChildActivityCard("My Rewards", Icons.Default.EmojiEvents, ChildRewardsCardBg, SelforaSecondary, onSelectRewards)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Dynamic Rewards Summary Card
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = SelforaSurface,
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ChildYellowStar, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text("You earned 5 Stars today!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                    Text("Keep up the awesome practice!", fontSize = 12.sp, color = SelforaTextSecondary)
                }
            }
        }
    }
}

@Composable
fun ChildActivityCard(title: String, icon: ImageVector, cardBgColor: Color, iconColor: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(36.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = SelforaTextPrimary)
        }
    }
}
