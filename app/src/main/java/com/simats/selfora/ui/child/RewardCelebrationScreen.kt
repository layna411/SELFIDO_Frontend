package com.simats.selfora.ui.child

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.simats.selfora.ui.theme.*

@Composable
fun RewardCelebrationScreen(
    onGoHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChildBlueCard)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = ChildYellowStar, modifier = Modifier.size(96.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Text("GREAT JOB AARAV!", fontWeight = FontWeight.Bold, fontSize = 28.sp, color = SelforaBlueDark)

                Spacer(modifier = Modifier.height(8.dp))

                Text("You finished all dressing steps today!", fontSize = 16.sp, color = SelforaTextDark, textAlign = TextAlign.Center)

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(5) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = ChildYellowStar, modifier = Modifier.size(36.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("+5 Super Stars Earned!", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = ChildOrangePlay)
            }
        }

        Button(
            onClick = onGoHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SelforaBluePrimary)
        ) {
            Icon(Icons.Default.Home, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("BACK TO MY HOME", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
        }
    }
}
