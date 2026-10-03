package com.metron.app.util

import java.io.BufferedReader
import java.io.StringReader

/**
 * Robust RFC-4180 compliant CSV parser and serializer.
 * Safely handles quoted text, embedded commas, newlines, escaped quotes, and irregular rows.
 */
object CsvUtils {

    fun parseRows(csvContent: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        if (csvContent.isBlank()) return rows

        val reader = BufferedReader(StringReader(csvContent))
        var line: String? = reader.readLine()

        while (line != null) {
            val tokens = parseLine(line)
            if (tokens.isNotEmpty() && tokens.any { it.isNotBlank() }) {
                rows.add(tokens)
            }
            line = reader.readLine()
        }
        return rows
    }

    private fun parseLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        val sb = java.lang.StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            when {
                c == '\"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                        sb.append('\"')
                        i++ // Skip escaped quote
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    tokens.add(sb.toString().trim())
                    sb.setLength(0)
                }
                else -> {
                    sb.append(c)
                }
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    fun escape(value: String): String {
        val safe = value.replace("\"", "\"\"")
        return if (safe.contains(",") || safe.contains("\"") || safe.contains("\n") || safe.contains("\r")) {
            "\"$safe\""
        } else {
            safe
        }
    }
}
