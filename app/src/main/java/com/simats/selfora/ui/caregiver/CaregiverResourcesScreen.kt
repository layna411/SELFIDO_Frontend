package com.simats.selfora.ui.caregiver

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.components.glass.GlassCard
import com.simats.selfora.ui.theme.*

data class ResourceTopic(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverResourcesScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current

    val resources = listOf(
        ResourceTopic(
            title = "ADL Activity Videos",
            subtitle = "Step-by-step videos for tooth brushing, dressing & spoon feeding",
            icon = Icons.Default.PlayCircle,
            accentColor = SelforaPrimary
        ),
        ResourceTopic(
            title = "Tips for Caregivers",
            subtitle = "Helpful occupational therapy strategies for home routines",
            icon = Icons.Default.Lightbulb,
            accentColor = Color(0xFFF59E0B)
        ),
        ResourceTopic(
            title = "Prompting Guide",
            subtitle = "How and when to fade verbal, visual & physical prompts",
            icon = Icons.Default.TrendingDown,
            accentColor = SelforaSecondary
        ),
        ResourceTopic(
            title = "Behavior Support Tips",
            subtitle = "Managing frustration, sensory challenges & encouraging motivation",
            icon = Icons.Default.Psychology,
            accentColor = Color(0xFFEC4899)
        ),
        ResourceTopic(
            title = "Frequently Asked Questions",
            subtitle = "Common pediatric OT questions answered by specialists",
            icon = Icons.Default.HelpOutline,
            accentColor = Color(0xFF0EA5E9)
        )
    )

    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            TopAppBar(
                title = { Text("Resources", fontWeight = FontWeight.Bold, color = SelforaTextPrimary) },
                modifier = Modifier.statusBarsPadding(),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SelforaSurface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBackground),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = SelforaPrimary.copy(alpha = 0.12f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📚", fontSize = 24.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Learning Library", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SelforaTextPrimary)
                            Text("Evidence-based guidance for parents & caregivers", fontSize = 12.sp, color = SelforaTextSecondary)
                        }
                    }
                }
            }

            items(resources.size) { idx ->
                val topic = resources[idx]
                Surface(
                    onClick = {
                        Toast.makeText(context, "Opening ${topic.title}", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White.copy(alpha = 0.92f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = topic.accentColor.copy(alpha = 0.12f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(topic.icon, contentDescription = null, tint = topic.accentColor, modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(topic.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SelforaTextPrimary)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(topic.subtitle, fontSize = 11.sp, color = SelforaTextSecondary)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = SelforaTextSecondary, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}
