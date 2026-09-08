package com.example

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

/**
 * Utility functions for formatting durations, file sizes, and media metadata.
 */
fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes % 60, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun formatTime(ms: Long): String {
    return formatDuration(ms)
}

fun formatFileSize(bytes: Long): String {
    val mb = bytes / (1024f * 1024f)
    return if (mb >= 1024f) {
        String.format("%.1f GB", mb / 1024f)
    } else {
        String.format("%.1f MB", mb)
    }
}

fun getSubtitleDisplayName(context: Context, uri: Uri): String {
    var name = "External Subtitle"
    try {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = cursor.getString(index)
                }
            }
        }
    } catch (_: Exception) {
        name = uri.lastPathSegment ?: "External Subtitle"
    }
    return name
}
