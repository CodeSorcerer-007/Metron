package com.metron.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metron.app.R
import com.metron.app.haptics.HapticsManager
import com.metron.app.theme.*

@Composable
fun OnboardingTour(
    onFinishTour: (currencyCode: String, currencySymbol: String, monthlyBudget: Double, haptics: Boolean, notifs: Boolean) -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = 5

    var selectedCurrencyCode by remember { mutableStateOf("INR") }
    var selectedCurrencySymbol by remember { mutableStateOf("₹") }
    var budgetAmountString by remember { mutableStateOf("30000") }
    var hapticsEnabled by remember { mutableStateOf(true) }
    var notifsEnabled by remember { mutableStateOf(true) }

    val currencies = listOf(
        Triple("INR", "₹", "Rupee (₹)"),
        Triple("USD", "$", "Dollar ($)"),
        Triple("EUR", "€", "Euro (€)"),
        Triple("GBP", "£", "Pound (£)"),
        Triple("AED", "AED", "Dirham (AED)")
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DarkBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Progress Dots & Skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Progress Dots
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 0 until totalPages) {
                        val isCurrent = i == currentPage
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (isCurrent) 24.dp else 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isCurrent) GoldPrimary else DarkSurfaceVariant)
                        )
                    }
                }

                if (currentPage < totalPages - 1) {
                    TextButton(onClick = {
                        HapticsManager.click()
                        currentPage = totalPages - 1
                    }) {
                        Text("Skip to Setup", color = GoldPrimary, fontSize = 12.sp)
                    }
                } else {
                    Spacer(modifier = Modifier.width(40.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Content Area with Animated Transition
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentPage,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> -width } + fadeOut()
                            )
                        } else {
                            (slideInHorizontally { width -> -width } + fadeIn()).togetherWith(
                                slideOutHorizontally { width -> width } + fadeOut()
                            )
                        }
                    },
                    label = "tourSlide"
                ) { page ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        when (page) {
                            // Page 0: Welcome to Metron
                            0 -> {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_metron_logo),
                                    contentDescription = "Metron Emblem",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(26.dp))
                                        .border(2.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(26.dp))
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "METRON • ΜΕΤΡΟΝ",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 2.sp,
                                    color = GoldPrimary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "“Μέτρον ἄριστον” — Measure is best",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarkTextPrimary
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "Welcome to a world-class personal finance companion. Metron is built on ancient Greek balance and modern Android precision — completely offline, deeply private, and exceptionally effortless.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = DarkTextSecondary,
                                    lineHeight = 22.sp
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    FeaturePill(Icons.Default.Security, "100% Offline & Private", "Zero internet permissions. Your money stays solely on this device.")
                                    FeaturePill(Icons.Default.Speed, "Zero Friction", "Record any expense in seconds with tactile hardware feedback.")
                                    FeaturePill(Icons.Default.SelfImprovement, "Financial Calm", "Designed to eliminate stress and cultivate mindful awareness.")
                                }
                            }

                            // Page 1: The Sanctuary & Daily Allowance
                            1 -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(32.dp))
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = "THE SANCTUARY",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp,
                                    color = GoldPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Your Daily Financial Harmony",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DarkTextPrimary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "The Sanctuary home screen answers your essential questions at a single glance, without confusing accounting jargon.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = DarkTextSecondary
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Visual Mock Card
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(GoldPrimary.copy(alpha = 0.4f), Color.Transparent)))
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(text = "DAILY SPENDING ALLOWANCE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldPrimary, letterSpacing = 1.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "₹1,250 / day remaining", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Metron calculates (Remaining Monthly Budget ÷ Days Left) so you always know exactly how much you can spend today without ever running out!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DarkTextSecondary
                                        )
                                    }
                                }
                            }

                            // Page 2: Zero Friction Quick Add & Merchant Memory
                            2 -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(36.dp))
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = "ZERO-FRICTION QUICK ADD",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp,
                                    color = GoldPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Record in Seconds, Not Minutes",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DarkTextPrimary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Text(
                                    text = "Tapping the center Golden (+) button opens the dedicated tactile sheet with an on-screen keypad and smart shortcuts.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center,
                                    color = DarkTextSecondary
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    FeaturePill(Icons.Default.Memory, "Merchant Memory", "Typing 'Coffee' or 'Swiggy' automatically auto-selects your category & payment account.")
                                    FeaturePill(Icons.Default.Vibration, "Hardware Haptics", "Every keypress and quick bump (+100, +500) resonates with physical click feedback.")
                                    FeaturePill(Icons.Default.Receipt, "Receipt Attachment", "Optionally attach a photo of your receipt stored securely on your phone.")
                                }
                            }

                            // Page 3: Ledger, Oracle & Cycles
                            3 -> {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.PieChart, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(32.dp))
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Text(
                                    text = "LEDGER, ORACLE & PILLARS",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp,
                                    color = GoldPrimary
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "Clarity & Vision Across Your Wealth",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DarkTextPrimary
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    FeaturePill(Icons.Default.ReceiptLong, "The Ledger", "Chronological history with universal search across merchants, notes, and categories.")
                                    FeaturePill(Icons.Default.AutoAwesome, "The Oracle", "Donut chart breakdown, cashflow balance, and Greek Spending Story narratives.")
                                    FeaturePill(Icons.Default.Shield, "The Pillars & Cycles", "Category budgets and subscription tracker with 1-tap 'Pay' recording.")
                                }
                            }

                            // Page 4: Personalize & Enter
                            4 -> {
                                Text(
                                    text = "PERSONALIZE YOUR MEASURE",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.5.sp,
                                    color = GoldPrimary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Set your standard before stepping into the sanctuary",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkTextSecondary
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                // Currency Selection
                                Text(text = "Choose Default Currency", style = MaterialTheme.typography.labelMedium, color = DarkTextSecondary, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    currencies.forEach { (code, sym, label) ->
                                        val isSel = selectedCurrencyCode == code
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSel) GoldPrimary else DarkSurface)
                                                .border(1.dp, if (isSel) GoldPrimary else DarkBorder, RoundedCornerShape(12.dp))
                                                .clickable {
                                                    HapticsManager.tick()
                                                    selectedCurrencyCode = code
                                                    selectedCurrencySymbol = sym
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(text = sym, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (isSel) DarkBackground else DarkTextPrimary)
                                                Text(text = code, fontSize = 10.sp, color = if (isSel) DarkBackground else DarkTextSecondary)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                // Monthly Budget Target
                                Text(text = "Monthly Spending Target", style = MaterialTheme.typography.labelMedium, color = DarkTextSecondary, modifier = Modifier.fillMaxWidth())
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = budgetAmountString,
                                    onValueChange = { budgetAmountString = it },
                                    label = { Text("Monthly Budget ($selectedCurrencySymbol)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldPrimary,
                                        unfocusedBorderColor = DarkBorder
                                    )
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                // Hardware & Software Features Toggles
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Vibration, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text("Hardware Tactile Haptics", style = MaterialTheme.typography.bodyMedium, color = DarkTextPrimary)
                                            }
                                            Switch(
                                                checked = hapticsEnabled,
                                                onCheckedChange = {
                                                    HapticsManager.tick()
                                                    hapticsEnabled = it
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = GoldDark)
                                            )
                                        }

                                        HorizontalDivider(color = DarkBorder.copy(alpha = 0.3f))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.Notifications, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text("Daily Mindful Reminders (8:30 PM)", style = MaterialTheme.typography.bodyMedium, color = DarkTextPrimary)
                                            }
                                            Switch(
                                                checked = notifsEnabled,
                                                onCheckedChange = {
                                                    HapticsManager.tick()
                                                    notifsEnabled = it
                                                },
                                                colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = GoldDark)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Buttons (Back & Continue / Enter)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentPage > 0) {
                    OutlinedButton(
                        onClick = {
                            HapticsManager.click()
                            currentPage--
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(GoldPrimary.copy(alpha = 0.5f)))
                    ) {
                        Text("Back", color = GoldPrimary, fontWeight = FontWeight.Bold)
                    }
                }

                Button(
                    onClick = {
                        HapticsManager.click()
                        if (currentPage < totalPages - 1) {
                            currentPage++
                        } else {
                            HapticsManager.success()
                            val budget = budgetAmountString.toDoubleOrNull() ?: 30000.0
                            onFinishTour(selectedCurrencyCode, selectedCurrencySymbol, budget, hapticsEnabled, notifsEnabled)
                        }
                    },
                    modifier = Modifier
                        .weight(if (currentPage > 0) 2f else 1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground)
                ) {
                    Text(
                        text = if (currentPage < totalPages - 1) "Next" else "Enter Sanctuary",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturePill(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = DarkTextPrimary)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = DarkTextSecondary)
        }
    }
}
