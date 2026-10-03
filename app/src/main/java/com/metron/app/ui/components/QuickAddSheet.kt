package com.metron.app.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metron.app.MetronApp
import com.metron.app.haptics.HapticsManager
import com.metron.app.model.*
import com.metron.app.ocr.ReceiptOcrParser
import com.metron.app.theme.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    currencySymbol: String,
    categories: List<Category>,
    accounts: List<Account>,
    recentMerchants: List<String>,
    onSaveTransaction: (Transaction) -> Unit,
    onSuggestCategory: (String) -> Category?,
    onSuggestAccount: (String) -> Account?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val repo = MetronApp.repository
    val scope = rememberCoroutineScope()

    var amountString by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var selectedCategory by remember {
        mutableStateOf(categories.firstOrNull { it.type == CategoryType.EXPENSE } ?: categories.firstOrNull())
    }
    var selectedAccount by remember { mutableStateOf(accounts.firstOrNull()) }
    var toAccount by remember { mutableStateOf(accounts.getOrNull(1)) }
    var merchantText by remember { mutableStateOf("") }
    var notesText by remember { mutableStateOf("") }
    var receiptPath by remember { mutableStateOf<String?>(null) }
    var showExtraDetails by remember { mutableStateOf(false) }

    var isOcrScanning by remember { mutableStateOf(false) }
    var ocrFeedbackText by remember { mutableStateOf<String?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isOcrScanning = true
                ocrFeedbackText = "Analyzing receipt offline..."
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val path = repo.saveReceiptImage(stream)
                        if (path != null) {
                            receiptPath = path
                        }
                    }

                    // Run on-device OCR
                    val ocrResult = ReceiptOcrParser.parseReceipt(context, uri)
                    if (ocrResult.amount != null && ocrResult.amount > 0.0) {
                        amountString = if (ocrResult.amount % 1.0 == 0.0) {
                            ocrResult.amount.toInt().toString()
                        } else {
                            String.format(Locale.US, "%.2f", ocrResult.amount)
                        }
                    }
                    if (!ocrResult.merchant.isNullOrBlank()) {
                        merchantText = ocrResult.merchant
                        val inferredCat = onSuggestCategory(ocrResult.merchant)
                        if (inferredCat != null) selectedCategory = inferredCat
                        val inferredAcc = onSuggestAccount(ocrResult.merchant)
                        if (inferredAcc != null) selectedAccount = inferredAcc
                    }

                    if (ocrResult.amount != null || ocrResult.merchant != null) {
                        ocrFeedbackText = "✨ Scanned: ${ocrResult.merchant ?: "Receipt"} ($currencySymbol${ocrResult.amount ?: ""})"
                        HapticsManager.success()
                    } else {
                        ocrFeedbackText = "Photo attached (details could not be auto-detected)"
                        HapticsManager.click()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    ocrFeedbackText = null
                } finally {
                    isOcrScanning = false
                }
            }
        }
    }

    val filteredCategories = remember(categories, selectedType) {
        when (selectedType) {
            TransactionType.EXPENSE -> categories.filter { it.type == CategoryType.EXPENSE || it.type == CategoryType.BOTH }
            TransactionType.INCOME -> categories.filter { it.type == CategoryType.INCOME || it.type == CategoryType.BOTH }
            TransactionType.TRANSFER -> categories
        }
    }

    LaunchedEffect(selectedType) {
        if (selectedCategory == null || (selectedCategory?.type != CategoryType.BOTH && selectedCategory?.type?.name != selectedType.name)) {
            selectedCategory = filteredCategories.firstOrNull()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = BronzeAccent.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Type Switcher: Expense | Income | Transfer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TransactionType.values().forEach { type ->
                    val isSelected = type == selectedType
                    val activeColor = when (type) {
                        TransactionType.EXPENSE -> SpartanRose
                        TransactionType.INCOME -> LaurelGreen
                        TransactionType.TRANSFER -> AegeanAzure
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) activeColor else Color.Transparent)
                            .clickable {
                                HapticsManager.click()
                                selectedType = type
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Large tactile Amount Display in Helvetica
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currencySymbol,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = BronzeAccent,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = if (amountString.isEmpty()) "0" else amountString,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (amountString.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f) else MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.6).sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Bump Chips (+50, +100, +500, +1000)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(50, 100, 500, 1000).forEach { bump ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, GoldBorderLight, RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable {
                                HapticsManager.tick()
                                val current = amountString.toDoubleOrNull() ?: 0.0
                                amountString = (current + bump).toInt().toString()
                            }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+$bump",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BronzeAccent
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Selector Chips
            if (selectedType != TransactionType.TRANSFER) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredCategories) { cat ->
                        val isSelected = cat.id == selectedCategory?.id
                        val catColor = parseColor(cat.colorHex)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) catColor else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    HapticsManager.tick()
                                    selectedCategory = cat
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = resolveIcon(cat.iconName),
                                contentDescription = null,
                                tint = if (isSelected) Color.White else catColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Merchant / Description with Smart Suggestion Memory
            OutlinedTextField(
                value = merchantText,
                onValueChange = { newText ->
                    merchantText = newText
                    val inferredCat = onSuggestCategory(newText)
                    if (inferredCat != null) selectedCategory = inferredCat
                    val inferredAcc = onSuggestAccount(newText)
                    if (inferredAcc != null) selectedAccount = inferredAcc
                },
                placeholder = { Text(if (selectedType == TransactionType.INCOME) "Source (e.g. Salary, Client)" else "Merchant (e.g. Coffee, Market)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BronzeAccent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            // Recent Merchant Memory Chips
            if (recentMerchants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(recentMerchants.take(5)) { merchant ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                                .clickable {
                                    HapticsManager.tick()
                                    merchantText = merchant
                                    val inferredCat = onSuggestCategory(merchant)
                                    if (inferredCat != null) selectedCategory = inferredCat
                                    val inferredAcc = onSuggestAccount(merchant)
                                    if (inferredAcc != null) selectedAccount = inferredAcc
                                }
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = merchant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Account Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (selectedType == TransactionType.TRANSFER) "From Account" else "Payment Method",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(accounts) { acc ->
                    val isSelected = acc.id == selectedAccount?.id
                    val accColor = parseColor(acc.colorHex)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) accColor else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                HapticsManager.tick()
                                selectedAccount = acc
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = resolveIcon(acc.iconName),
                            contentDescription = null,
                            tint = if (isSelected) Color.White else accColor,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = acc.name,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Transfer To Account Selector
            if (selectedType == TransactionType.TRANSFER) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "To Account",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accounts.filter { it.id != selectedAccount?.id }) { acc ->
                        val isSelected = acc.id == toAccount?.id
                        val accColor = parseColor(acc.colorHex)
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) accColor else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    HapticsManager.tick()
                                    toAccount = acc
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = resolveIcon(acc.iconName),
                                contentDescription = null,
                                tint = if (isSelected) Color.White else accColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = acc.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Expandable Notes & Receipt with Offline OCR
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        HapticsManager.tick()
                        showExtraDetails = !showExtraDetails
                    }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (showExtraDetails) "Hide notes & receipt" else "+ Add note & receipt photo (Offline OCR)",
                    fontSize = 12.sp,
                    color = BronzeAccent,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (showExtraDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = BronzeAccent,
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = showExtraDetails) {
                Column(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = notesText,
                        onValueChange = { notesText = it },
                        placeholder = { Text("Notes (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BronzeAccent,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    // OCR Scanning Feedback Badge
                    if (isOcrScanning) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(AegeanAzureContainer)
                                .padding(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = AegeanAzure)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyzing receipt offline with ML Kit...", fontSize = 11.sp, color = AegeanAzure, fontWeight = FontWeight.Medium)
                        }
                    } else if (!ocrFeedbackText.isNullOrBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(LaurelGreenContainer)
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = LaurelGreen, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(ocrFeedbackText!!, fontSize = 11.sp, color = LaurelGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Receipt attachment button / preview
                    if (!receiptPath.isNullOrBlank() && File(receiptPath!!).exists()) {
                        val bitmap = remember(receiptPath) {
                            com.metron.app.util.ImageUtils.loadThumbnail(receiptPath, 160)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp)
                        ) {
                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Receipt Preview",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Receipt Attached",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = LaurelGreen
                                )
                                Text(
                                    text = "Stored privately on device",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = {
                                    repo.deleteReceiptImage(receiptPath)
                                    receiptPath = null
                                    ocrFeedbackText = null
                                    HapticsManager.click()
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove receipt",
                                    tint = SpartanRose,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                HapticsManager.click()
                                photoPickerLauncher.launch("image/*")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp), tint = BronzeAccent)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Scan Receipt Photo (Offline OCR)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BronzeAccent)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tactile Custom Keypad
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val keypadRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf(".", "0", "⌫")
                )

                for (row in keypadRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (key in row) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, LightBorder, RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
                                    .clickable {
                                        HapticsManager.tick()
                                        when (key) {
                                            "⌫" -> {
                                                if (amountString.isNotEmpty()) {
                                                    amountString = amountString.dropLast(1)
                                                }
                                            }
                                            "." -> {
                                                if (!amountString.contains(".")) {
                                                    amountString = if (amountString.isEmpty()) "0." else "$amountString."
                                                }
                                            }
                                            else -> {
                                                if (amountString.length < 9) {
                                                    amountString += key
                                                }
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = key,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Record / Save Button
            val isValid = (amountString.toDoubleOrNull() ?: 0.0) > 0 && selectedAccount != null
            Button(
                onClick = {
                    val rawAmount = amountString.toDoubleOrNull() ?: 0.0
                    val amount = com.metron.app.util.MoneyUtils.round(rawAmount)
                    if (amount > 0 && selectedAccount != null) {
                        val fallbackMerchant = if (selectedType == TransactionType.TRANSFER) "Transfer to ${toAccount?.name ?: "Account"}"
                            else selectedCategory?.name ?: "Expense"
                        val finalMerchant = merchantText.trim().take(100).ifBlank { fallbackMerchant }

                        val tx = Transaction(
                            amount = amount,
                            type = selectedType,
                            categoryId = selectedCategory?.id ?: 1L,
                            accountId = selectedAccount!!.id,
                            toAccountId = if (selectedType == TransactionType.TRANSFER) toAccount?.id else null,
                            merchant = finalMerchant,
                            notes = notesText.trim().take(500),
                            receiptPath = receiptPath,
                            timestamp = System.currentTimeMillis()
                        )
                        HapticsManager.success()
                        onSaveTransaction(tx)
                        onDismiss()
                    }
                },
                enabled = isValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BronzeAccent,
                    contentColor = Color.White,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Record Measure",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
