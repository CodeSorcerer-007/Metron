package com.metron.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun resolveIcon(name: String): ImageVector {
    return when (name) {
        "Restaurant" -> Icons.Default.Restaurant
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "ShoppingBag" -> Icons.Default.ShoppingBag
        "Home" -> Icons.Default.Home
        "ReceiptLong" -> Icons.Default.ReceiptLong
        "ConfirmationNumber" -> Icons.Default.ConfirmationNumber
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "MenuBook" -> Icons.Default.MenuBook
        "SmartDisplay" -> Icons.Default.SmartDisplay
        "Spa" -> Icons.Default.Spa
        "Flight" -> Icons.Default.Flight
        "AutoAwesome" -> Icons.Default.AutoAwesome
        "Payments" -> Icons.Default.Payments
        "Work" -> Icons.Default.Work
        "TrendingUp" -> Icons.Default.TrendingUp
        "CardGiftcard" -> Icons.Default.CardGiftcard
        "Savings" -> Icons.Default.Savings
        "AccountBalance" -> Icons.Default.AccountBalance
        "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
        "CreditCard" -> Icons.Default.CreditCard
        "QrCodeScanner" -> Icons.Default.QrCodeScanner
        else -> Icons.Default.Category
    }
}

fun parseColor(hex: String, defaultColor: Color = Color(0xFFD4AF37)): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun CategoryIconBox(
    iconName: String,
    colorHex: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    iconSize: Dp = 22.dp,
    isCircle: Boolean = false
) {
    val color = parseColor(colorHex)
    val shape = if (isCircle) CircleShape else RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = resolveIcon(iconName),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(iconSize)
        )
    }
}
