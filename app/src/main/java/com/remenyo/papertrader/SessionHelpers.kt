package com.remenyo.papertrader

import androidx.compose.runtime.Composable
import java.text.SimpleDateFormat

fun roundAndCap(d: Double): Int {
    val percent = d.toInt()
    return if (percent < 100) percent else 100
}

@Composable
fun completenessPercentText(startTS: Long, endTS: Long, currentTS: Long): String {
    return if (endTS != startTS)
        "${roundAndCap((100 - ((endTS.toDouble() - currentTS.toDouble()) / (endTS.toDouble() - startTS.toDouble()) * 100.0)))}%"
    else
        "?%"
}

@Composable
fun shortTimeFromUnixTimestamp(ts: Long): String {
    val time = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
    return time.format(ts * 1000L)
}

@Composable
fun dateFromUnixTimestamp(ts: Long): String {
    val date = SimpleDateFormat.getDateInstance()
    return date.format(ts * 1000L)
}

@Composable
fun dateTimeFromUnixTimestamp(ts: Long): Pair<String, String> {
    return Pair(dateFromUnixTimestamp(ts), shortTimeFromUnixTimestamp(ts))
}

@Composable
fun longTimeFromUnixTimestamp(ts: Long): String {
    val time = SimpleDateFormat.getTimeInstance()
    return time.format(ts * 1000L)
}

@Composable
fun dateTimeText(startTS: Long, endTS: Long): String {
    val (startDate, startTime) = dateTimeFromUnixTimestamp(startTS)
    val (endDate, endTime) = dateTimeFromUnixTimestamp(endTS)
    return if (startDate == endDate)
        "$startDate $startTime - $endTime"
    else "$startDate $startTime - $endDate $endTime"
}