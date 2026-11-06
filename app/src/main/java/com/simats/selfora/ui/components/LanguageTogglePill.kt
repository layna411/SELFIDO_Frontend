package com.simats.selfora.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.theme.SelforaPrimary
import com.simats.selfora.utils.LanguageManager

@Composable
fun LanguageTogglePill(
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val currentLanguage by LanguageManager.currentLanguage.collectAsState()
    val isTamil = currentLanguage == "ta"

    val bgColor by animateColorAsState(
        targetValue = if (isTamil) Color(0xFFFF9800) else SelforaPrimary,
        animationSpec = tween(250),
        label = "langPillBg"
    )

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable {
                LanguageManager.toggleLanguage()
            },
        shape = RoundedCornerShape(20.dp),
        color = bgColor.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, bgColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isCompact) 8.dp else 12.dp,
                vertical = if (isCompact) 4.dp else 6.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Switch Language",
                tint = bgColor,
                modifier = Modifier.size(if (isCompact) 14.dp else 16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isTamil) "தமிழ்" else if (isCompact) "EN" else "English",
                color = bgColor,
                fontWeight = FontWeight.Bold,
                fontSize = if (isCompact) 11.sp else 12.sp
            )
        }
    }
}

