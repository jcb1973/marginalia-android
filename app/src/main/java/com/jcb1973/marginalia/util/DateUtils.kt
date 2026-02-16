package com.jcb1973.marginalia.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private val shortFormat: DateFormat
        get() = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault())

    private val relativeThresholdMs = 7 * 24 * 60 * 60 * 1000L // 7 days

    fun formatShort(epochMillis: Long): String {
        return shortFormat.format(Date(epochMillis))
    }

    fun formatRelative(epochMillis: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - epochMillis
        return when {
            diff < 60_000 -> "Just now"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            diff < 86_400_000 -> "${diff / 3_600_000}h ago"
            diff < relativeThresholdMs -> "${diff / 86_400_000}d ago"
            else -> formatShort(epochMillis)
        }
    }

    fun parseShort(text: String): Long? {
        return try {
            shortFormat.parse(text)?.time
        } catch (_: Exception) {
            try {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(text)?.time
            } catch (_: Exception) {
                null
            }
        }
    }
}
