package com.max.mediaplayer

import java.util.Locale

/**
 * 格式化时间，将MS为单位的时间数字改成可读性更高的字符串形式：00:00:00
 */
fun formatTime(position: Int): String {
    val totalSeconds = position / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
}