package com.metron.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.metron.app.MetronApp
import com.metron.app.haptics.HapticsManager
import com.metron.app.model.Account
import com.metron.app.model.AccountType
import com.metron.app.theme.*
import com.metron.app.ui.components.CategoryIconBox
import com.metron.app.ui.components.parseColor

@Composable
fun VaultScreen(
    accounts: List<Account>,
    currencySymbol: String,
    currencyCode: String,
    themeMode: String,
    isCalmMode: Boolean,
    onSetCurrency: (String, String) -> Unit,
    onSetThemeMode: (String) -> Unit,
    onToggleCalmMode: () -> Unit,
    onAddAccount: (Account) -> Unit,
    onExportJson: () -> String,
    onImportJson: (String) -> Boolean,
    onExportCsv: () -> String,
    onGenerateDemoData: () -> Unit,
    onClearAllData: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repo = MetronApp.repository

    val isHapticsEnabled by repo.isHapticsEnabled.collectAsState()
    val isNotificationsEnabled by repo.isNotificationsEnabled.collectAsState()
    val dailyReminderHour by repo.dailyReminderHour.collectAsState()
    val dailyReminderMinute by repo.dailyReminderMinute.collectAsState()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var exportDataString by remember { mutableStateOf("") }
    var importInputString by remember { mutableStateOf("") }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            repo.setNotificationsEnabled(true)
            HapticsManager.success()
            Toast.makeText(context, "Daily mindful check-in reminders active!", Toast.LENGTH_SHORT).show()
        } else {
            repo.setNotificationsEnabled(false)
            Toast.makeText(context, "Notification permission needed for reminders", Toast.LENGTH_SHORT).show()
        }
    }

    val totalNetWorth = remember(accounts) {
        accounts.sumOf { it.currentBalance }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "THE TREASURY & VAULT",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    color = GoldPrimary
                )
                Text(
                    text = "Accounts, offline privacy, backup & settings",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Net Worth Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Net Liquid Wealth",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currencySymbol${String.format("%,.2f", totalNetWorth)}",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Aggregated across all ${accounts.size} active accounts",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Accounts Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payment Accounts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    TextButton(onClick = { showAddAccountDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldPrimary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Account", color = GoldPrimary, fontSize = 12.sp)
                    }
                }
            }

            items(accounts, key = { it.id }) { acc ->
                val accColor = parseColor(acc.colorHex)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryIconBox(
                                iconName = acc.iconName,
                                colorHex = acc.colorHex,
                                size = 40.dp,
                                iconSize = 20.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = acc.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text(text = acc.type.title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Text(
                            text = "$currencySymbol${String.format("%,.2f", acc.currentBalance)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (acc.currentBalance >= 0) MaterialTheme.colorScheme.onSurface else SpartanRose
                        )
                    }
                }
            }

            // Offline Privacy Pledge Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LaurelGreen.copy(alpha = 0.08f)),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(LaurelGreen.copy(alpha = 0.3f)))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = LaurelGreen, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Zero Internet. Pure Privacy.",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = LaurelGreen
                            )
                            Text(
                                text = "Metron requires 0 network permissions. All transactions, accounts, and insights reside exclusively on your physical device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Customization Options
            item {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Currency Selector Row
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCurrencyDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Currency", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                        Text("$currencyCode ($currencySymbol)", style = MaterialTheme.typography.bodyMedium, color = GoldPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Theme Selector Row
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showThemeDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Theme Style", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        }
                        Text(
                            text = when (themeMode) {
                                "ATHENIAN_LIGHT" -> "Athenian Light"
                                "OLED_BLACK" -> "OLED Pure Black"
                                else -> "Aegean Dark"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = GoldPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Financial Calm Mode Toggle Row
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SelfImprovement, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Financial Calm Mode", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text("Minimalist view with zero distracting statistics", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isCalmMode,
                            onCheckedChange = {
                                HapticsManager.tick()
                                onToggleCalmMode()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = GoldDark)
                        )
                    }
                }
            }

            // Hardware Vibrations & Tactile Haptics Switch
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Hardware Tactile Haptics", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text("Vibrations on keypad, buttons & transaction actions", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = isHapticsEnabled,
                            onCheckedChange = {
                                repo.setHapticsEnabled(it)
                                if (it) HapticsManager.success()
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = GoldDark)
                        )
                    }
                }
            }

            // Daily Mindful Reminder Notification
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Daily Mindful Check-in", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text("Offline local alarm reminding you to balance your ledger", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(
                                checked = isNotificationsEnabled,
                                onCheckedChange = { enable ->
                                    if (enable) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                                                repo.setNotificationsEnabled(true)
                                                HapticsManager.success()
                                            } else {
                                                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                        } else {
                                            repo.setNotificationsEnabled(true)
                                            HapticsManager.success()
                                        }
                                    } else {
                                        repo.setNotificationsEnabled(false)
                                        HapticsManager.click()
                                    }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = GoldPrimary, checkedTrackColor = GoldDark)
                            )
                        }

                        if (isNotificationsEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            val amPm = if (dailyReminderHour >= 12) "PM" else "AM"
                            val displayHour = if (dailyReminderHour % 12 == 0) 12 else dailyReminderHour % 12
                            val displayMin = String.format("%02d", dailyReminderMinute)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable {
                                        HapticsManager.tick()
                                        android.app.TimePickerDialog(
                                            context,
                                            { _, h, m ->
                                                repo.setDailyReminderTime(h, m)
                                                HapticsManager.click()
                                            },
                                            dailyReminderHour,
                                            dailyReminderMinute,
                                            false
                                        ).show()
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Scheduled Reminder Time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$displayHour:$displayMin $amPm (Tap to change)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Revisit Metron Guide / Interactive Tour
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            HapticsManager.click()
                            repo.setOnboardingCompleted(false)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Revisit Metron Guide & Tour", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text("Replay the interactive onboarding introduction", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Data & Backup Section Header
            item {
                Text(
                    text = "Data Sovereignty & Backup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Backup JSON / CSV Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            exportDataString = onExportJson()
                            showExportDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export JSON", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showImportDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import JSON", fontSize = 12.sp)
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        val csv = onExportCsv()
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Metron CSV", csv))
                        Toast.makeText(context, "Full CSV copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy CSV to Clipboard", fontSize = 12.sp)
                }
            }

            // Pre-populate Demo Data Button
            item {
                Button(
                    onClick = {
                        onGenerateDemoData()
                        Toast.makeText(context, "Demo data populated with realistic expenses!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary.copy(alpha = 0.2f),
                        contentColor = GoldPrimary
                    )
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Populate Demo Transactions", fontWeight = FontWeight.Bold)
                }
            }

            // Clear Data Button
            item {
                TextButton(
                    onClick = { showClearConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset All Data & Clear Database", color = SpartanRose, fontSize = 12.sp)
                }
            }

            // Greek Heritage Footer
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "METRON • ΜΕΤΡΟΝ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Crafted with Hellenic harmony & modern Android engineering.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "v1.0.0 • 100% Offline & Private",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(70.dp))
            }
        }
    }

    // Currency Picker Dialog
    if (showCurrencyDialog) {
        val currencies = listOf(
            Triple("INR", "₹", "Indian Rupee"),
            Triple("USD", "$", "US Dollar"),
            Triple("EUR", "€", "Euro"),
            Triple("GBP", "£", "British Pound"),
            Triple("JPY", "¥", "Japanese Yen"),
            Triple("AED", "AED", "UAE Dirham"),
            Triple("SGD", "S$", "Singapore Dollar"),
            Triple("AUD", "A$", "Australian Dollar")
        )

        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Select Currency") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    currencies.forEach { (code, sym, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (currencyCode == code) GoldPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable {
                                    onSetCurrency(code, sym)
                                    showCurrencyDialog = false
                                }
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("$name ($code)", fontWeight = if (currencyCode == code) FontWeight.Bold else FontWeight.Normal)
                            Text(sym, fontWeight = FontWeight.Bold, color = GoldPrimary)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Close") }
            }
        )
    }

    // Theme Picker Dialog
    if (showThemeDialog) {
        val themes = listOf(
            Pair("AEGEAN_DARK", "Aegean Dark (Obsidian & Gold)"),
            Pair("ATHENIAN_LIGHT", "Athenian Light (Marble & Bronze)"),
            Pair("OLED_BLACK", "OLED Pure Black (High Contrast)")
        )

        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Visual Theme Style") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    themes.forEach { (key, title) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (themeMode == key) GoldPrimary.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable {
                                    onSetThemeMode(key)
                                    showThemeDialog = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = themeMode == key, onClick = {
                                onSetThemeMode(key)
                                showThemeDialog = false
                            })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(title)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Exported Data (JSON)") },
            text = {
                Column {
                    Text(
                        text = "Your complete encrypted/structured local backup is ready. You can copy it to preserve your records.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = exportDataString.take(400) + if (exportDataString.length > 400) "..." else "",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().height(140.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Metron Backup JSON", exportDataString))
                    Toast.makeText(context, "Backup JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
                    showExportDialog = false
                }) {
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Done") }
            }
        )
    }

    // Import Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Restore From JSON") },
            text = {
                Column {
                    Text("Paste previously exported JSON backup content here to restore all data:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = importInputString,
                        onValueChange = { importInputString = it },
                        modifier = Modifier.fillMaxWidth().height(140.dp),
                        placeholder = { Text("{ \"version\": 1, ... }") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (importInputString.isNotBlank()) {
                        val success = onImportJson(importInputString)
                        if (success) {
                            Toast.makeText(context, "Database restored successfully!", Toast.LENGTH_SHORT).show()
                            showImportDialog = false
                        } else {
                            Toast.makeText(context, "Invalid JSON format", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Account Dialog
    if (showAddAccountDialog) {
        var accNameText by remember { mutableStateOf("") }
        var accBalText by remember { mutableStateOf("0") }
        var accType by remember { mutableStateOf(AccountType.BANK) }

        AlertDialog(
            onDismissRequest = { showAddAccountDialog = false },
            title = { Text("Add Payment Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = accNameText,
                        onValueChange = { accNameText = it },
                        label = { Text("Account Name (e.g. HDFC, Savings)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = accBalText,
                        onValueChange = { accBalText = it },
                        label = { Text("Opening Balance ($currencySymbol)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val bal = accBalText.toDoubleOrNull() ?: 0.0
                    if (accNameText.isNotBlank()) {
                        onAddAccount(
                            Account(
                                name = accNameText,
                                type = accType,
                                initialBalance = bal,
                                colorHex = "#38BDF8",
                                iconName = "AccountBalance"
                            )
                        )
                    }
                    showAddAccountDialog = false
                }) {
                    Text("Add Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAccountDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Clear Confirmation Dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Clear All Financial Data?") },
            text = { Text("This will permanently delete all transactions, budgets, and recurring items from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        Toast.makeText(context, "Database cleared.", Toast.LENGTH_SHORT).show()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SpartanRose)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) { Text("Cancel") }
            }
        )
    }
}
