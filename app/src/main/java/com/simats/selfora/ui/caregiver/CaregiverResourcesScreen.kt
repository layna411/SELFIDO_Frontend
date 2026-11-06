package com.simats.selfora.ui.caregiver

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.components.glass.GlassTopBar
import com.simats.selfora.ui.theme.*
import com.simats.selfora.utils.LanguageManager

data class ResourceTopic(
    val id: String,
    val titleEn: String,
    val titleTa: String,
    val subtitleEn: String,
    val subtitleTa: String,
    val icon: ImageVector,
    val accentColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverResourcesScreen(
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isTamil = currentLanguage == "ta"

    var selectedTopic by remember { mutableStateOf<ResourceTopic?>(null) }

    val resources = remember {
        listOf(
            ResourceTopic(
                id = "ADL_VIDEOS",
                titleEn = "ADL Activity Visual Guides",
                titleTa = "ADL செயல்பாட்டு வழிகாட்டிகள்",
                subtitleEn = "Step-by-step visual routines for T-shirt, shirt, jacket & oral care",
                subtitleTa = "டி-சர்ட், சட்டை, ஜாக்கெட் மற்றும் பல் துலக்குதல் படிநிலை வழிகாட்டிகள்",
                icon = Icons.Default.PlayCircle,
                accentColor = SelforaPrimary
            ),
            ResourceTopic(
                id = "PROMPTING_GUIDE",
                titleEn = "Prompting & Fading Hierarchy",
                titleTa = "உதவிக்குறிப்புகள் & குறைப்பு முறை",
                subtitleEn = "How and when to fade from physical to verbal & independent cues",
                subtitleTa = "உடல் உதவிகளிலிருந்து சைகை மற்றும் சுதந்திர நிலைக்கு மாற்றுவது எப்படி",
                icon = Icons.Default.TrendingDown,
                accentColor = SelforaSecondary
            ),
            ResourceTopic(
                id = "DAILY_TIPS",
                titleEn = "Tips for Daily Home Routines",
                titleTa = "பெற்றோருக்கான தினசரி குறிப்புகள்",
                subtitleEn = "Evidence-based occupational therapy strategies for morning routines",
                subtitleTa = "காலை மற்றும் மாலை நேரங்களுக்கான தொழில்முறை சிகிச்சை குறிப்புகள்",
                icon = Icons.Default.Lightbulb,
                accentColor = Color(0xFFF59E0B)
            ),
            ResourceTopic(
                id = "BEHAVIOR_SUPPORT",
                titleEn = "Sensory & Behavior Support",
                titleTa = "உணர்வு மற்றும் நடத்தை ஆதரவு",
                subtitleEn = "Managing sensory sensitivity to clothing textures, seams & tags",
                subtitleTa = "ஆடை தையல், லேபிள் மற்றும் உணர்வு உணர்திறனை கையாளுதல்",
                icon = Icons.Default.Psychology,
                accentColor = Color(0xFFEC4899)
            ),
            ResourceTopic(
                id = "FAQ",
                titleEn = "Frequently Asked Questions",
                titleTa = "அடிக்கடி கேட்கப்படும் கேள்விகள்",
                subtitleEn = "Pediatric occupational therapy Q&A answered by specialists",
                subtitleTa = "சிகிச்சையாளர்களால் பதிலளிக்கப்பட்ட பொதுவான கேள்விகள்",
                icon = Icons.Default.HelpOutline,
                accentColor = Color(0xFF0EA5E9)
            )
        )
    }

    Scaffold(
        containerColor = SelforaBackground,
        topBar = {
            GlassTopBar(
                title = if (isTamil) "வளங்கள் & வழிகாட்டல்கள்" else "Caregiver Resources",
                subtitle = if (isTamil) "ஆதார அடிப்படையிலான சிகிச்சை வழிகாட்டல்கள்" else "Evidence-Based Occupational Therapy Guidance",
                accentColor = SelforaPrimary,
                showLanguageToggle = true
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(SelforaBackground),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Overview Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.5.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SelforaPrimary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, SelforaPrimary.copy(alpha = 0.25f)),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = SelforaPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTamil) "பெற்றோர் கற்றல் நூலகம்" else "Clinical Learning Library",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SelforaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isTamil) "வீட்டுப் பயிற்சிக்கான மருத்துவ நிபுணர்களின் வழிகாட்டல்கள்" else "Specialized guidance designed for home ADL practice routines",
                                fontSize = 12.sp,
                                color = SelforaTextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Topic List Cards
            itemsIndexed(resources, key = { index, topic -> "${topic.id}_$index" }) { _, topic ->
                Surface(
                    onClick = { selectedTopic = topic },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    shadowElevation = 1.5.dp
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
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = topic.icon,
                                        contentDescription = null,
                                        tint = topic.accentColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isTamil) topic.titleTa else topic.titleEn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.5.sp,
                                    color = SelforaTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (isTamil) topic.subtitleTa else topic.subtitleEn,
                                    fontSize = 11.5.sp,
                                    color = SelforaTextSecondary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View",
                            tint = SelforaTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Interactive Detailed Resource Bottom Sheet / Modal
    if (selectedTopic != null) {
        val topic = selectedTopic!!
        ModalBottomSheet(
            onDismissRequest = { selectedTopic = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .padding(bottom = 36.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = topic.accentColor.copy(alpha = 0.14f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = topic.icon,
                                    contentDescription = null,
                                    tint = topic.accentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isTamil) topic.titleTa else topic.titleEn,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = SelforaTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isTamil) "மருத்துவ நிபுணர் வழிகாட்டி" else "Occupational Therapy Clinical Guide",
                                fontSize = 11.sp,
                                color = SelforaTextSecondary
                            )
                        }
                    }

                    IconButton(onClick = { selectedTopic = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = SelforaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color(0xFFE2E8F0))
                Spacer(modifier = Modifier.height(16.dp))

                // Topic-specific Detailed Interactive Content
                when (topic.id) {
                    "ADL_VIDEOS" -> {
                        AdlVideosResourceContent(isTamil)
                    }
                    "PROMPTING_GUIDE" -> {
                        PromptingGuideResourceContent(isTamil)
                    }
                    "DAILY_TIPS" -> {
                        DailyTipsResourceContent(isTamil)
                    }
                    "BEHAVIOR_SUPPORT" -> {
                        BehaviorSupportResourceContent(isTamil)
                    }
                    "FAQ" -> {
                        FaqResourceContent(isTamil)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { selectedTopic = null },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = topic.accentColor)
                ) {
                    Text(
                        text = if (isTamil) "புரிந்தது • முடிந்தது" else "Got It • Done",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun AdlVideosResourceContent(isTamil: Boolean) {
    val activities = listOf(
        Triple("👕 " + if (isTamil) "டி-சர்ட் அணிதல்" else "T-Shirt Dressing", "20 " + if (isTamil) "படிகள் • காலர் முதல் வழிகாட்டல்" else "Steps • Backward Chaining sequence", Color(0xFF2563EB)),
        Triple("👔 " + if (isTamil) "சட்டை & பொத்தான்கள்" else "Shirt & Buttoning", "23 " + if (isTamil) "படிகள் • கை மற்றும் பொத்தான் வரிசை" else "Steps • Visual cuff & button sequencing", Color(0xFF0D9488)),
        Triple("🧥 " + if (isTamil) "ஜாக்கெட் & ஜிப்பர்" else "Jacket & Zipper", "26 " + if (isTamil) "படிகள் • ஜிப் இணைப்பு & இழுத்தல்" else "Steps • Motor planning & zipper latching", Color(0xFF7C3AED)),
        Triple("🩲 " + if (isTamil) "உள்ளாடை / பனியன்" else "Undergarment & Vest", "18 " + if (isTamil) "படிகள் • இரு கால்கள் மற்றும் இடுப்பு நிலை" else "Steps • Leg alignment & waist pull sequence", Color(0xFFEA580C)),
        Triple("🪥 " + if (isTamil) "பல் துலக்குதல்" else "Oral Hygiene & Brushing", "5 " + if (isTamil) "படிகள் • 2 நிமிட டைமர் வழிகாட்டல்" else "Steps • 2-minute oral hygiene routine", Color(0xFF059669))
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = if (isTamil) "முக்கிய செயல்பாட்டு வரிசைகள்:" else "Core ADL Dressing Routines:",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = SelforaTextPrimary
        )

        activities.forEach { (title, subtitle, color) ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, color.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                        Text(text = subtitle, fontSize = 11.5.sp, color = SelforaTextSecondary)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = color,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = if (isTamil) "செயலில்" else "Active",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptingGuideResourceContent(isTamil: Boolean) {
    val levels = listOf(
        Pair("Level 0: Independent (சுயாதீனம்)", if (isTamil) "குழந்தை எந்த உதவியும் இன்றி சுயமாக படிநிலையை முடிக்கிறது." else "Child performs the step independently with zero cues."),
        Pair("Level 1: Verbal Cue (வாய்மொழி)", if (isTamil) "மென்மையான வழிகாட்டும் சொல் ('கையை உட்செலுத்து')." else "Gentle spoken cue or direction ('Push your hand through')."),
        Pair("Level 2: Gestural Cue (சைகை)", if (isTamil) "சட்டை கை அல்லது காலரை விரலால் சுட்டிக்காட்டுதல்." else "Pointing at the sleeve, opening, or nodding direction."),
        Pair("Level 3: Visual Cue (காட்சி)", if (isTamil) "படிநிலை படம் அல்லது GIF வழிகாட்டியை காட்டுதல்." else "Showing the step card, video model, or GIF preview."),
        Pair("Level 4: Physical Prompt (உடல் உதவி)", if (isTamil) "கையை பிடித்து மெதுவாக வழிநடத்துதல் (Hand-over-hand)." else "Gentle hand-over-hand physical assistance.")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFEF3C7),
            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Timer, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isTamil) "3-விநாடி விதி: அடுத்த உதவிக்குறிப்பை அளிக்கும் முன் குழந்தை சுயமாக முயற்சி செய்ய 3-5 விநாடிகள் காத்திருக்கவும்." else "The 3-Second Wait Rule: Always wait 3-5 seconds after giving a cue to allow the child to process motor planning before escalating.",
                    fontSize = 11.5.sp,
                    color = Color(0xFF92400E),
                    lineHeight = 16.sp
                )
            }
        }

        levels.forEach { (lvl, desc) ->
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = lvl, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SelforaPrimary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = desc, fontSize = 11.5.sp, color = SelforaTextSecondary, lineHeight = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun DailyTipsResourceContent(isTamil: Boolean) {
    val tips = listOf(
        Pair("⏰ " + if (isTamil) "நிலையான நேரம் & இடம்" else "Consistent Timing & Environment", if (isTamil) "தினமும் ஒரே அறையில் மற்றும் வழக்கமான நேரத்தில் ஆடை அணிதலை பயிற்சி செய்யுங்கள்." else "Practice dressing at the exact same time and room every morning to build neural muscle memory."),
        Pair("🏷️ " + if (isTamil) "ஆடை தேர்வு & மாற்றங்கள்" else "Clothing Adaptations", if (isTamil) "அரிப்பு லேபிள்களை வெட்டவும்; தளர்வான பருத்தி ஆடைகள் மற்றும் மீள் இடுப்புப் பட்டைகளை பயன்படுத்தவும்." else "Cut off itchy brand tags, choose soft breathable cotton, and use elastic waistbands to reduce frustration."),
        Pair("🔗 " + if (isTamil) "பின்நோக்கிய சங்கிலி முறை (Backward Chaining)" else "Backward Chaining Technique", if (isTamil) "கடைசி படியை (சட்டையை கீழே இழுப்பது) குழந்தை செய்ய விடுங்கள், இதனால் வெற்றி உணர்வு கிடைக்கும்." else "Have your child complete the final rewarding step (pulling down hem) so they experience immediate accomplishment."),
        Pair("⭐ " + if (isTamil) "உடனடி பாராட்டு & ஊக்குவிப்பு" else "Immediate Positive Reinforcement", if (isTamil) "ஒவ்வொரு படியை முடித்த உடனும் ஹை-ஃபைவ் அல்லது புன்னகையுடன் பாராட்டுங்கள்." else "Praise effort instantly after each completed step with specific praise ('Super job pushing your arm through!').")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        tips.forEach { (title, desc) ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = SelforaTextPrimary)
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = desc, fontSize = 11.5.sp, color = SelforaTextSecondary, lineHeight = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun BehaviorSupportResourceContent(isTamil: Boolean) {
    val behaviors = listOf(
        Pair("🧘 " + if (isTamil) "உணர்வு உணர்திறன் கையாளுதல்" else "Sensory Sensitivity & Desensitization", if (isTamil) "ஆடையை அணிவதற்கு முன் குழந்தையின் தோலில் மெதுவாக தடவி பழக வைக்கவும்." else "Let your child touch and explore the fabric before wearing it; warm up stiff winter clothes with hands."),
        Pair("📋 " + if (isTamil) "காட்சி அட்டவணை (Visual Schedules)" else "Visual Step Predictability", if (isTamil) "பயிற்சிக்கு முன் SELFORA படிநிலை அட்டையை காட்டி அடுத்த கட்டத்தை முன்கூட்டியே தெரியப்படுத்தவும்." else "Show the upcoming steps on the SELFORA app so the child feels in control and knows when dressing ends."),
        Pair("👕 " + if (isTamil) "சுய தேர்வு சுதந்திரம் (Choice Making)" else "Encourage Autonomy Through Choices", if (isTamil) "இரண்டு சட்டைகளில் ஒன்றை தேர்ந்தெடுக்க விடுங்கள், இது எதிர்ப்பை குறைக்கும்." else "Present two acceptable shirts and let your child choose their favorite to eliminate power struggles."),
        Pair("⏸️ " + if (isTamil) "அமைதி இடைவேளைகள் (Calm Breaks)" else "Take Calm Reset Breaks", if (isTamil) "குழந்தை வருத்தமடைந்தால், ஆழமாக மூச்சு விட்டு அமைதியான பின் மீண்டும் தொடங்குங்கள்." else "If emotional distress occurs, pause calmly, take deep breaths together, and resume without pressure.")
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        behaviors.forEach { (title, desc) ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFDF2F8),
                border = BorderStroke(1.dp, Color(0xFFFBCFE8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9D174D))
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(text = desc, fontSize = 11.5.sp, color = SelforaTextSecondary, lineHeight = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun FaqResourceContent(isTamil: Boolean) {
    val faqs = listOf(
        Pair(
            if (isTamil) "கே: தினமும் எவ்வளவு நேரம் ஆடை அணிதல் பயிற்சி செய்ய வேண்டும்?" else "Q: How often should we practice ADL dressing daily?",
            if (isTamil) "பதில்: தினமும் காலை அல்லது மாலையில் 10-15 நிமிடங்கள் வழக்கமான ஆடை மாற்றும் நேரத்தில் பயிற்சி செய்வது நரம்பியல் வளர்ச்சியை துரிதப்படுத்தும்." else "A: Daily 10-15 minutes of structured practice during natural morning/evening routines builds the strongest motor memory."
        ),
        Pair(
            if (isTamil) "கே: குழந்தை காலர் இறுக்கமாக உள்ளதாக அழுதால் என்ன செய்வது?" else "Q: What if my child refuses tight collar openings?",
            if (isTamil) "பதில்: காலர் அகலமான வி-நெக் அல்லது பொத்தான் உள்ள சட்டைகளை முதலில் பயன்படுத்தவும். படிப்படியாக வழக்கமான சட்டைகளுக்கு மாற்றலாம்." else "A: Start with wide stretch collars or open button shirts, then transition gradually once upper-body tolerance improves."
        ),
        Pair(
            if (isTamil) "கே: உதவிக்குறிப்பை எப்போது குறைக்க வேண்டும்?" else "Q: When should I fade down to a lower prompt level?",
            if (isTamil) "பதில்: குழந்தை தொடர்ந்து 3 முறை ஒரு படியில் வெற்றிகரமாக செய்தால், உடனடியாக அடுத்த குறைந்த உதவிக்கு (எ.கா: உடல் உதவியிலிருந்து சைகைக்கு) மாறவும்." else "A: When your child successfully completes a step 3 consecutive times with current prompt, fade to the next lower prompt level."
        ),
        Pair(
            if (isTamil) "கே: குடும்பத்தினர் அனைவரும் ஒரே முறையை பின்பற்ற வேண்டுமா?" else "Q: Should all family members follow the same steps?",
            if (isTamil) "பதில்: ஆம், பெற்றோர், தாத்தா-பாட்டி அனைவரும் ஒரே சொல் மற்றும் படிநிலையை பின்பற்றுவது குழந்தைக்கு குழப்பமின்றி விரைவான வெற்றியை தரும்." else "A: Yes! Consistency across all caregivers (parents, grandparents) is the single biggest predictor of fast ADL independence."
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        faqs.forEach { (q, a) ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF0F9FF),
                border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = q, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = a, fontSize = 11.5.sp, color = SelforaTextSecondary, lineHeight = 16.sp)
                }
            }
        }
    }
}
