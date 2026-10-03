package com.metron.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metron.app.theme.*

@Composable
fun SpendingStoryCard(
    storyText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(
                1.dp,
                ParchmentBorder,
                RoundedCornerShape(22.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = ParchmentBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = BronzeAccent,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "THE SPENDING STORY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = BronzeAccent
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = storyText,
                style = MaterialTheme.typography.bodyMedium,
                lineHeight = 22.sp,
                color = ParchmentText
            )
        }
    }
}
