package com.metron.app.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metron.app.MetronApp
import com.metron.app.haptics.HapticsManager
import com.metron.app.model.*
import com.metron.app.theme.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionSheet(
    transaction: Transaction,
    categories: List<Category>,
    accounts: List<Account>,
    currencySymbol: String,
    onUpdateTransaction: (Transaction) -> Unit,
    onDeleteTransaction: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val repo = MetronApp.repository

    var amountString by remember { mutableStateOf(transaction.amount.toString()) }
    var selectedType by remember { mutableStateOf(transaction.type) }
    var selectedCategory by remember { mutableStateOf(categories.find { it.id == transaction.categoryId } ?: categories.firstOrNull()) }
    var selectedAccount by remember { mutableStateOf(accounts.find { it.id == transaction.accountId } ?: accounts.firstOrNull()) }
    var merchantText by remember { mutableStateOf(transaction.merchant) }
    var notesText by remember { mutableStateOf(transaction.notes) }
    var receiptPath by remember { mutableStateOf(transaction.receiptPath) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val path = repo.saveReceiptImage(stream)
                    if (path != null) {
                        receiptPath = path
                        HapticsManager.click()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle(color = GoldPrimary.copy(alpha = 0.5f)) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Transaction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = {
                    HapticsManager.warning()
                    onDeleteTransaction(transaction.id)
                    onDismiss()
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = SpartanRose)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Amount field
            OutlinedTextField(
                value = amountString,
                onValueChange = { amountString = it },
                label = { Text("Amount ($currencySymbol)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Merchant / Title
            OutlinedTextField(
                value = merchantText,
                onValueChange = { merchantText = it },
                label = { Text("Merchant / Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Category Selector
            Text("Category", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat.id == selectedCategory?.id
                    val catColor = parseColor(cat.colorHex)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) catColor else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                HapticsManager.tick()
                                selectedCategory = cat
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(cat.name, fontSize = 12.sp, color = if (isSelected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Account Selector
            Text("Payment Method", style = MaterialTheme.typography.labelMedium)
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) accColor else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                HapticsManager.tick()
                                selectedAccount = acc
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(acc.name, fontSize = 12.sp, color = if (isSelected) androidx.compose.ui.graphics.Color.White else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Notes field
            OutlinedTextField(
                value = notesText,
                onValueChange = { notesText = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Receipt Section
            Text("Receipt Image", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(6.dp))

            if (!receiptPath.isNullOrBlank() && File(receiptPath!!).exists()) {
                val bitmap = remember(receiptPath) {
                    BitmapFactory.decodeFile(receiptPath)
                }
                if (bitmap != null) {
                    Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp))) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Receipt Preview",
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = {
                                repo.deleteReceiptImage(receiptPath)
                                receiptPath = null
                                HapticsManager.click()
                            },
                            modifier = Modifier.align(Alignment.TopEnd).size(28.dp).background(SpartanRose.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Attach Receipt Photo", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val amt = amountString.toDoubleOrNull() ?: transaction.amount
                    if (amt > 0 && selectedCategory != null && selectedAccount != null) {
                        HapticsManager.success()
                        val updated = transaction.copy(
                            amount = amt,
                            merchant = merchantText.ifBlank { selectedCategory!!.name },
                            categoryId = selectedCategory!!.id,
                            accountId = selectedAccount!!.id,
                            notes = notesText,
                            receiptPath = receiptPath
                        )
                        onUpdateTransaction(updated)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = DarkBackground)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
