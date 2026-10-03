package com.metron.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metron.app.haptics.HapticsManager
import com.metron.app.model.TimeFilter
import com.metron.app.theme.*
import java.util.*

@Composable
fun PrimarySpendingCard(
    totalSpent: Double,
    totalIncome: Double,
    currencySymbol: String,
    selectedFilter: TimeFilter,
    onFilterSelected: (TimeFilter) -> Unit,
    monthlyBudget: Double,
    monthlySpent: Double,
    isCalmMode: Boolean,
    modifier: Modifier = Modifier
) {
    val cal = remember { Calendar.getInstance() }
    val daysInMonth = remember { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }
    val currentDay = remember { cal.get(Calendar.DAY_OF_MONTH) }
    val daysRemaining = remember { (daysInMonth - currentDay + 1).coerceAtLeast(1) }

    val remainingBudget = (monthlyBudget - monthlySpent).coerceAtLeast(0.0)
    val dailyAllowance = if (daysRemaining > 0) remainingBudget / daysRemaining else 0.0
    val budgetProgress = if (monthlyBudget > 0) (monthlySpent / monthlyBudget).toFloat().coerceIn(0f, 1f) else 0f

    val progressColor by animateColorAsState(
        targetValue = when {
            budgetProgress > 0.95f -> SpartanRose
            budgetProgress > 0.8f -> AmberWarning
            else -> GoldPrimary
        },
        label = "budgetColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    listOf(
                        BronzeAccent.copy(alpha = 0.35f),
                        GoldPrimary.copy(alpha = 0.15f),
                        BronzeAccent.copy(alpha = 0.25f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Period Selector Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(TimeFilter.TODAY, TimeFilter.THIS_WEEK, TimeFilter.THIS_MONTH, TimeFilter.ALL_TIME).forEach { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(11.dp))
                            .background(
                                if (isSelected) BronzeAccent else Color.Transparent
                            )
                            .clickable {
                                HapticsManager.tick()
                                onFilterSelected(filter)
                            }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = filter.title,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Contextual Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isCalmMode) "YOUR MEASURE" else "OUTFLOW • ${selectedFilter.title.uppercase()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                if (monthlyBudget > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(LaurelGreenContainer)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "$daysRemaining days left",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = LaurelGreen
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Spending Amount
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = currencySymbol,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = BronzeAccent,
                    modifier = Modifier.padding(bottom = 5.dp, end = 4.dp)
                )
                AnimatedContent(targetState = totalSpent, label = "spentAnim") { spent ->
                    Text(
                        text = String.format(Locale.getDefault(), "%,.2f", spent),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.6).sp
                    )
                }
            }

            // Daily Spending Allowance Highlight Card
            if (monthlyBudget > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(ParchmentBg)
                        .border(1.dp, ParchmentBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = BronzeAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Daily Spending Allowance",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ParchmentText
                            )
                            Text(
                                text = "To stay in harmony for remaining $daysRemaining days",
                                fontSize = 9.5.sp,
                                color = ParchmentText.copy(alpha = 0.75f)
                            )
                        }
                    }

                    Text(
                        text = "$currencySymbol${String.format(Locale.getDefault(), "%,.0f", dailyAllowance)}/day",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BronzeAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Inflow vs Outflow Mini-Stats
            if (!isCalmMode) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Outflow Pill
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SpartanRoseContainer)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = null,
                            tint = SpartanRose,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "Outflow", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = SpartanRose)
                            Text(
                                text = "$currencySymbol${String.format(Locale.getDefault(), "%,.0f", totalSpent)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Inflow Pill
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(LaurelGreenContainer)
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = LaurelGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(text = "Inflow", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = LaurelGreen)
                            Text(
                                text = "$currencySymbol${String.format(Locale.getDefault(), "%,.0f", totalIncome)}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Monthly Budget Progress Bar
            if (monthlyBudget > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = progressColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (remainingBudget > 0) {
                                    "$currencySymbol${String.format(Locale.getDefault(), "%,.0f", remainingBudget)} left of $currencySymbol${String.format(Locale.getDefault(), "%,.0f", monthlyBudget)}"
                                } else {
                                    "Measure exceeded by $currencySymbol${String.format(Locale.getDefault(), "%,.0f", -remainingBudget)}"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "${(budgetProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = progressColor
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { budgetProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = progressColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}
