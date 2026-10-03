package com.metron.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.metron.app.haptics.HapticsManager
import com.metron.app.model.Account
import com.metron.app.model.Category
import com.metron.app.model.Transaction
import com.metron.app.model.TransactionType
import com.metron.app.theme.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionListItem(
    transaction: Transaction,
    category: Category?,
    account: Account?,
    currencySymbol: String,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showReceiptDialog by remember { mutableStateOf(false) }

    val amountColor = when (transaction.type) {
        TransactionType.EXPENSE -> SpartanRose
        TransactionType.INCOME -> LaurelGreen
        TransactionType.TRANSFER -> AegeanAzure
    }

    val amountPrefix = when (transaction.type) {
        TransactionType.EXPENSE -> "-"
        TransactionType.INCOME -> "+"
        TransactionType.TRANSFER -> "⇄ "
    }

    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val timeStr = timeFormat.format(Date(transaction.timestamp))

    val hasReceipt = !transaction.receiptPath.isNullOrBlank() && File(transaction.receiptPath).exists()

    // Full screen receipt dialog
    if (showReceiptDialog && hasReceipt) {
        val bitmap = remember(transaction.receiptPath) {
            BitmapFactory.decodeFile(transaction.receiptPath)
        }
        if (bitmap != null) {
            Dialog(onDismissRequest = { showReceiptDialog = false }) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Receipt: ${transaction.merchant}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { showReceiptDialog = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Full Receipt",
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable {
                HapticsManager.tick()
                expanded = !expanded
            }
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            CategoryIconBox(
                iconName = category?.iconName ?: "Category",
                colorHex = category?.colorHex ?: "#D4AF37",
                size = 42.dp,
                iconSize = 20.dp
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Merchant & Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = transaction.merchant.ifEmpty { category?.name ?: "Expense" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (hasReceipt) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = "Receipt attached",
                            tint = GoldPrimary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = category?.name ?: "General",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )

                    Text(
                        text = account?.name ?: "Account",
                        style = MaterialTheme.typography.labelSmall,
                        color = parseColor(account?.colorHex ?: "#38BDF8").copy(alpha = 0.9f)
                    )

                    Text(
                        text = "•",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )

                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Amount
            Text(
                text = "$amountPrefix$currencySymbol${String.format("%,.2f", transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }

        // Expanded Actions Bar (Receipt preview, Duplicate, Edit, Delete)
        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                if (transaction.notes.isNotBlank()) {
                    Text(
                        text = "Note: ${transaction.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                if (hasReceipt) {
                    val thumb = remember(transaction.receiptPath) {
                        BitmapFactory.decodeFile(transaction.receiptPath)
                    }
                    if (thumb != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    HapticsManager.tick()
                                    showReceiptDialog = true
                                }
                                .padding(6.dp)
                        ) {
                            Image(
                                bitmap = thumb.asImageBitmap(),
                                contentDescription = "Receipt Thumbnail",
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "View Receipt Photo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = GoldPrimary
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Repeat Action
                    TextButton(
                        onClick = {
                            HapticsManager.tick()
                            expanded = false
                            onDuplicate()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = GoldPrimary)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Repeat", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Edit Action
                    TextButton(
                        onClick = {
                            HapticsManager.tick()
                            expanded = false
                            onEdit()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = AegeanAzure)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Edit", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Delete Action
                    TextButton(
                        onClick = {
                            HapticsManager.warning()
                            expanded = false
                            onDelete()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = SpartanRose)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Delete", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
