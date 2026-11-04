package com.simats.selfora.ui.therapist

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.data.model.ChildDto
import com.simats.selfora.ui.theme.*

// ============================================================================
// 1. TOP HEADER (iPhone-inspired clean clinical header with status bar insets)
// ============================================================================
@Composable
fun StartSessionTopBar(
    childId: Long,
    currentDate: String,
    therapistName: String,
    therapistDesignation: String = "Occupational Therapist (OT)",
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, spotColor = Color(0x18172033)),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back button & Title Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Circular back button
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .size(40.dp)
                            .clickable(onClick = onBackClick)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SelforaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Start New Session",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "Child ID: #$childId • Date: $currentDate",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = SelforaTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Compact Therapist Information Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SelforaPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("👨‍⚕️", fontSize = 12.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            val cleanName = therapistName.ifBlank { "Jenkins" }
                            Text(
                                text = if (cleanName.contains("Dr.", ignoreCase = true)) cleanName else "Dr. $cleanName",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "OT Specialist",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = SelforaPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. PATIENT CONTEXT CARD (Profile details & Interactive Gender Switcher)
// ============================================================================
@Composable
fun PatientContextCard(
    childDto: ChildDto?,
    childId: Long,
    childGender: String?, // "BOY", "GIRL", or null
    onSelectGender: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Patient details row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    val avatarEmoji = when (childGender) {
                        "GIRL" -> "👧"
                        "BOY" -> "👦"
                        else -> "🧒"
                    }
                    val avatarBg = when (childGender) {
                        "GIRL" -> SelforaSecondary.copy(alpha = 0.14f)
                        "BOY" -> SelforaPrimary.copy(alpha = 0.12f)
                        else -> Color(0xFFF1F5F9)
                    }

                    Surface(
                        shape = CircleShape,
                        color = avatarBg,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(avatarEmoji, fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        val displayName = childDto?.let {
                            "${it.firstName} ${it.lastName ?: ""}".trim()
                        }?.ifBlank { null } ?: "Child #$childId"

                        val displayAge = childDto?.dateOfBirth?.let {
                            try {
                                val birth = java.time.LocalDate.parse(it)
                                java.time.Period.between(birth, java.time.LocalDate.now()).years
                            } catch (_: Exception) { 7 }
                        } ?: 7

                        Text(
                            text = displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Age: $displayAge yrs • ${if (childGender == "GIRL") "Female" else if (childGender == "BOY") "Male" else "Unspecified"} • ASD",
                            fontSize = 12.sp,
                            color = SelforaTextSecondary
                        )
                    }
                }

                // Dual-Gender Pathway Selector
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "PATHWAY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaTextSecondary,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Boy selector
                        val isBoy = childGender == "BOY"
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (isBoy) SelforaPrimary else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isBoy) SelforaPrimary else Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable { onSelectGender("BOY") }
                        ) {
                            Text(
                                text = "👦 Boy",
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBoy) Color.White else SelforaTextPrimary
                            )
                        }

                        // Girl selector
                        val isGirl = childGender == "GIRL"
                        Surface(
                            shape = RoundedCornerShape(9.dp),
                            color = if (isGirl) SelforaSecondary else Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, if (isGirl) SelforaSecondary else Color(0xFFCBD5E1)),
                            modifier = Modifier.clickable { onSelectGender("GIRL") }
                        ) {
                            Text(
                                text = "👧 Girl",
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isGirl) Color.White else SelforaTextPrimary
                            )
                        }
                    }
                }
            }

            // Unassigned gender warning if applicable
            if (childGender == null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gender is unassigned in profile. Showing common Footwear and Food activities. Select Boy or Girl above to view dressing.",
                            fontSize = 11.sp,
                            color = Color(0xFF92400E),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// 3. ADL CATEGORY SELECTOR (Dresses, Footwear, Food + Compact View Switcher)
// ============================================================================
@Composable
fun AdlCategorySelector(
    selectedCategory: String, // "DRESSING", "SHOES", "EATING"
    onSelectCategory: (String) -> Unit,
    isShelfLayout: Boolean,
    onToggleLayout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header with Compact Grid/Shelf Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "SELECT ADL CATEGORY",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = SelforaPrimary,
                    letterSpacing = 0.5.sp
                )
            }

            // Compact View Mode Toggle
            Surface(
                shape = RoundedCornerShape(9.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shadowElevation = 1.dp,
                modifier = Modifier.clickable(onClick = onToggleLayout)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = if (isShelfLayout) Icons.Default.GridView else Icons.Default.ViewCarousel,
                        contentDescription = "Toggle View",
                        tint = SelforaPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isShelfLayout) "Grid View" else "Shelf View",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3 Balanced Category Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CategoryCard(
                title = "Dresses",
                icon = "👗",
                isSelected = selectedCategory == "DRESSING",
                onClick = { onSelectCategory("DRESSING") },
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                title = "Footwear",
                icon = "👟",
                isSelected = selectedCategory == "SHOES",
                onClick = { onSelectCategory("SHOES") },
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                title = "Food",
                icon = "🍽️",
                isSelected = selectedCategory == "EATING",
                onClick = { onSelectCategory("EATING") },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CategoryCard(
    title: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1.0f,
        animationSpec = tween(180, easing = FastOutSlowInEasing),
        label = "CategoryScale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .height(78.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFFF0F7FF) else Color.White,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) SelforaPrimary else Color(0xFFE2E8F0)
        ),
        shadowElevation = if (isSelected) 2.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) SelforaPrimary.copy(alpha = 0.16f) else Color(0xFFF1F5F9),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = icon, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) SelforaPrimary else SelforaTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================================
// 4. SECTION HEADER (Upper Body, Lower Body, Footwear, Eating)
// ============================================================================
@Composable
fun AdlSectionHeader(
    title: String,
    icon: String,
    activityCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = SelforaPrimary.copy(alpha = 0.12f),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(icon, fontSize = 18.sp)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = title.uppercase(),
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaTextPrimary,
                letterSpacing = 0.5.sp
            )
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = SelforaPrimary.copy(alpha = 0.08f),
            border = BorderStroke(0.8.dp, SelforaPrimary.copy(alpha = 0.2f))
        ) {
            Text(
                text = "$activityCount Activities",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaPrimary,
                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
            )
        }
    }
}

// ============================================================================
// 5. ACTIVITY SELECTION CARD (Clean, iPhone-inspired activity tile)
// ============================================================================
@Composable
fun ActivitySelectionCard(
    number: Int,
    activity: SelfidoActivityDef,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.01f else 1.0f,
        animationSpec = tween(150),
        label = "ActivityCardScale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .height(138.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) Color(0xFFF0F7FF) else Color.White,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) SelforaPrimary else Color(0xFFE2E8F0)
        ),
        shadowElevation = if (isSelected) 2.5.dp else 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            // Top Row: Avatar Circle on left, Steps Tag + Blue Checkmark on right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isSelected) SelforaPrimary.copy(alpha = 0.16f) else Color(0xFFF1F5F9),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = activity.icon, fontSize = 21.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) SelforaPrimary.copy(alpha = 0.12f) else Color(0xFFF8FAFC),
                        border = BorderStroke(
                            0.8.dp,
                            if (isSelected) SelforaPrimary.copy(alpha = 0.35f) else Color(0xFFCBD5E1)
                        )
                    ) {
                        Text(
                            text = "${activity.totalSteps} steps",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) SelforaPrimary else Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    if (isSelected) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Surface(
                            shape = CircleShape,
                            color = SelforaPrimary,
                            modifier = Modifier.size(19.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Middle & Bottom: Activity title (English) & Tamil translation
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "$number. ${activity.title.uppercase()}",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) SelforaPrimary else SelforaTextPrimary,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = activity.titleTa.ifBlank { " " },
                    fontSize = 11.sp,
                    color = if (isSelected) SelforaPrimary.copy(alpha = 0.85f) else SelforaTextSecondary,
                    maxLines = 1,
                    minLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}



// ============================================================================
// 6. SELECTED ACTIVITY OVERVIEW & START SESSION CTA CARD
// ============================================================================
@Composable
fun SelectedActivityOverviewCard(
    activity: SelfidoActivityDef?,
    childGender: String?,
    selectedCategory: String,
    onStartSessionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.5.dp, SelforaPrimary.copy(alpha = 0.4f)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "SELECTED ADL FOR THERAPY SESSION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SelforaPrimary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = SelforaPrimary.copy(alpha = 0.12f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(activity?.icon ?: "🎯", fontSize = 26.sp)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = activity?.title ?: "Select an Activity",
                        fontSize = 16.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SelforaTextPrimary
                    )
                    if (!activity?.titleTa.isNullOrBlank()) {
                        Text(
                            text = activity!!.titleTa,
                            fontSize = 12.sp,
                            color = SelforaTextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${activity?.totalSteps ?: 20} Steps • Gender: ${childGender ?: "Common"} • $selectedCategory",
                        fontSize = 11.5.sp,
                        color = SelforaPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (!activity?.startingPosition.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🎯 Clinical Starting Position:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = SelforaPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activity!!.startingPosition,
                            fontSize = 12.sp,
                            color = SelforaTextPrimary,
                            lineHeight = 17.sp
                        )
                        if (!activity.startingPositionTa.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = activity.startingPositionTa,
                                fontSize = 11.5.sp,
                                color = SelforaTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onStartSessionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SelforaPrimary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🚀", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Therapy Session",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
