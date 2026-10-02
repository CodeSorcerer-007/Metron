package com.metron.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metron.app.R
import com.metron.app.theme.GoldPrimary

@Composable
fun OnboardingDialog(
    onComplete: (currencyCode: String, currencySymbol: String, monthlyBudget: Double) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var selectedCurrencyCode by remember { mutableStateOf("INR") }
    var selectedCurrencySymbol by remember { mutableStateOf("₹") }
    var budgetAmountString by remember { mutableStateOf("30000") }

    val currencies = listOf(
        Triple("INR", "₹", "Rupee (₹)"),
        Triple("USD", "$", "Dollar ($)"),
        Triple("EUR", "€", "Euro (€)"),
        Triple("GBP", "£", "Pound (£)"),
        Triple("AED", "AED", "Dirham (AED)")
    )

    AlertDialog(
        onDismissRequest = { /* Force user to complete */ },
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Step 1: Welcome Statement
                if (step == 1) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_metron_logo),
                        contentDescription = "Metron Emblem",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(18.dp))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "METRON",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 2.sp,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Μέτρον ἄριστον\nMeasure is best.",
                        style = MaterialTheme.typography.titleSmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "A tranquil, 100% offline personal finance sanctuary designed to keep you completely aware of your money in seconds.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Step 2: Choose Currency
                if (step == 2) {
                    Text(
                        text = "Select Default Currency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "All balances and expenses will use this standard.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currencies.forEach { (code, sym, label) ->
                            val isSelected = selectedCurrencyCode == code
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) GoldPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        selectedCurrencyCode = code
                                        selectedCurrencySymbol = sym
                                    }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = sym,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                            }
                        }
                    }
                }

                // Step 3: Optional Monthly Spending Measure
                if (step == 3) {
                    Text(
                        text = "Monthly Spending Measure",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Set an initial monthly target to cultivate calm discipline (optional, can be modified anytime).",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = budgetAmountString,
                        onValueChange = { budgetAmountString = it },
                        label = { Text("Monthly Budget ($selectedCurrencySymbol)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (step < 3) {
                        step++
                    } else {
                        val budget = budgetAmountString.toDoubleOrNull() ?: 30000.0
                        onComplete(selectedCurrencyCode, selectedCurrencySymbol, budget)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = MaterialTheme.colorScheme.background
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (step < 3) "Continue" else "Enter Sanctuary",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            if (step > 1) {
                TextButton(onClick = { step-- }) {
                    Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}
