package com.metron.app.ocr

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class OcrReceiptResult(
    val amount: Double? = null,
    val merchant: String? = null,
    val dateString: String? = null,
    val rawText: String = "",
    val confidenceNotes: String = ""
)

object ReceiptOcrParser {

    private val recognizer by lazy {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }

    /**
     * Process a receipt bitmap completely on-device with zero network calls.
     */
    suspend fun parseReceipt(context: Context, bitmap: Bitmap): OcrReceiptResult {
        val image = InputImage.fromBitmap(bitmap, 0)
        return processInputImage(image)
    }

    /**
     * Process a receipt Uri completely on-device.
     */
    suspend fun parseReceipt(context: Context, uri: Uri): OcrReceiptResult {
        val image = InputImage.fromFilePath(context, uri)
        return processInputImage(image)
    }

    private suspend fun processInputImage(image: InputImage): OcrReceiptResult =
        suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val result = extractDetailsFromVisionText(visionText.text)
                    continuation.resume(result)
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    continuation.resume(
                        OcrReceiptResult(
                            rawText = "",
                            confidenceNotes = "OCR Scan failed: ${exception.localizedMessage}"
                        )
                    )
                }
        }

    /**
     * High-precision heuristics to parse receipts:
     * 1. Detect grand totals / amounts.
     * 2. Detect merchant name (usually in top lines, skipping noise).
     * 3. Detect date.
     */
    fun extractDetailsFromVisionText(rawText: String): OcrReceiptResult {
        if (rawText.isBlank()) return OcrReceiptResult()

        val lines = rawText.lines().map { it.trim() }.filter { it.isNotBlank() }

        var detectedAmount: Double? = null
        var detectedMerchant: String? = null
        var detectedDate: String? = null

        // 1. Merchant Detection: Look at first 4 lines, ignoring generic invoice words
        val ignoredMerchantKeywords = listOf(
            "tax invoice", "receipt", "invoice", "cash bill", "bill", "order", "welcome", "gstin", "tel", "phone"
        )
        for (line in lines.take(5)) {
            val lower = line.lowercase()
            val containsIgnored = ignoredMerchantKeywords.any { lower.contains(it) }
            val hasDigitsOnly = line.all { it.isDigit() || it.isWhitespace() || it == '-' }
            if (!containsIgnored && !hasDigitsOnly && line.length >= 3 && detectedMerchant == null) {
                // Clean up any extraneous symbols
                detectedMerchant = line.replace(Regex("[^a-zA-Z0-9 &.,'\"-]"), "").trim()
            }
        }

        // 2. Amount Detection: Scan for Total / Grand Total / Net Amount / Balance Due
        val totalPatterns = listOf(
            Regex("""(?i)\bgrand\s*total[^\d\n]*[:=]?\s*[$€£₹Rs.]*\s*([0-9]+[.,][0-9]{2})"""),
            Regex("""(?i)\b(?:total\s*amount|total\s*due|net\s*amount|balance\s*due)[^\d\n]*[:=]?\s*[$€£₹Rs.]*\s*([0-9]+[.,][0-9]{2})"""),
            Regex("""(?i)(?<!sub)\btotal[^\d\n]*[:=]?\s*[$€£₹Rs.]*\s*([0-9]+[.,][0-9]{2})"""),
            Regex("""(?i)\bgrand\s*total[^\d\n]*[:=]?\s*[$€£₹Rs.]*\s*([0-9]+)"""),
            Regex("""(?i)\b(?:total\s*amount|total\s*due|net\s*amount|balance\s*due)[^\d\n]*[:=]?\s*[$€£₹Rs.]*\s*([0-9]+)"""),
            Regex("""(?i)(?<!sub)\btotal[^\d\n]*[:=]?\s*[$€£₹Rs.]*\s*([0-9]+)"""),
            Regex("""[$€£₹]\s*([0-9]+[.,][0-9]{2})"""),
            Regex("""(?:Rs|INR)\.?\s*([0-9]+[.,][0-9]{2})""")
        )

        for (pattern in totalPatterns) {
            val match = pattern.find(rawText)
            if (match != null) {
                val groupVal = match.groupValues.getOrNull(1)?.replace(",", ".")
                val parsed = groupVal?.toDoubleOrNull()
                if (parsed != null && parsed > 0.0) {
                    detectedAmount = parsed
                    break
                }
            }
        }

        // Fallback: If no "Total" keyword matched, find the largest floating point number in bottom half
        if (detectedAmount == null && lines.isNotEmpty()) {
            val numberRegex = Regex("""\b([0-9]{1,6}[.,][0-9]{2})\b""")
            val candidateAmounts = mutableListOf<Double>()
            for (line in lines.takeLast(lines.size / 2 + 1)) {
                for (match in numberRegex.findAll(line)) {
                    val num = match.value.replace(",", ".").toDoubleOrNull()
                    if (num != null && num > 0.0) candidateAmounts.add(num)
                }
            }
            if (candidateAmounts.isNotEmpty()) {
                detectedAmount = candidateAmounts.maxOrNull()
            }
        }

        // 3. Date Detection
        val dateRegex = Regex("""\b(\d{1,2}[/-]\d{1,2}[/-]\d{2,4}|\d{4}[/-]\d{1,2}[/-]\d{1,2})\b""")
        val dateMatch = dateRegex.find(rawText)
        if (dateMatch != null) {
            detectedDate = dateMatch.value
        }

        return OcrReceiptResult(
            amount = detectedAmount,
            merchant = detectedMerchant,
            dateString = detectedDate,
            rawText = rawText,
            confidenceNotes = if (detectedAmount != null) "Detected total: $detectedAmount" else "Amount not recognized with high confidence"
        )
    }
}
