package com.remenyo.papertrader.db.room

import android.util.Log
import com.remenyo.papertrader.*
import com.remenyo.papertrader.db.PreferenceKeys
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.math.max
import kotlin.math.min


object CandleRepo {

    const val TAG = "PaperTrader_CandleRepo"

    // Ugly and over-engineered - Business logic go brr

    private val rangeSerializer = ListSerializer(TSRange.serializer())

    private fun getOfflineAvailableRanges(): List<TSRange> {
        return Json.decodeFromString(
            rangeSerializer, App.KVStore.decodeString(
                PreferenceKeys.availableRangeList_string, "[]"
            ) ?: "[]"
        )
    }

    private fun getSortedRelevantRanges(startTS: Long, endTS: Long): List<TSRange> =
        getOfflineAvailableRanges().filter { it.startTS <= endTS && it.endTS >= startTS }
            .sortedBy { it.startTS }


    // add this range to ranges
    private fun updateAvailableRanges(startTS: Long, endTS: Long) {
        val allRanges = mutableListOf<TSRange>()
        allRanges.addAll(getOfflineAvailableRanges())
        val relevantRanges = getSortedRelevantRanges(startTS, endTS)

        if (relevantRanges.isNotEmpty()) {
            // API 24 bruh
            allRanges.removeIf { relevantRanges.contains(it) }

            allRanges.add(
                TSRange(
                    min(startTS, relevantRanges.first().startTS),
                    max(endTS, relevantRanges.last().endTS)
                )
            )
        } else allRanges.add(TSRange(startTS, endTS))

        App.KVStore.encode(
            PreferenceKeys.availableRangeList_string,
            Json.encodeToString(rangeSerializer, allRanges)
        )
    }

    // Calculates ranges which needed to be fetched from network
    private fun calculateNetworkRanges(startTS: Long, endTS: Long): List<TSRange> {
        var debugCount: Long = 0

        val neededRanges = mutableListOf<TSRange>()

        val relevantRanges = getSortedRelevantRanges(
            startTS, endTS
        )

        if (relevantRanges.isNotEmpty()) {

            var increment = startTS
            for (r in relevantRanges) {
                if (increment < r.startTS) neededRanges.add(TSRange(increment, r.endTS))
                increment = r.endTS
            }
            relevantRanges.last().endTS.let { if (it < endTS) neededRanges.add(TSRange(it, endTS)) }

        } else neededRanges.add(TSRange(startTS, endTS))

        neededRanges.forEach { debugCount += (it.endTS - it.startTS).floorDiv(60) }
        Log.d(
            TAG,
            "~$debugCount candles are needed from network (session=${(endTS - startTS).floorDiv(60)})"
        )

        return neededRanges
    }

    private suspend fun loadFromNetwork(ranges: List<TSRange>): Pair<List<OHLCV>, Boolean> {
        val candles = mutableListOf<OHLCV>()
        try {
            for (r in ranges) {
                candles.addAll(API.loadCandles(r.startTS, r.endTS))
            }
            return Pair(candles, true)
        } catch (e: Exception) {
            Log.e(TAG, e.toString(), e)
            AnalyticsHelper.reportException(e)
        }
        return Pair(candles, false)
    }

    suspend fun loadNewCandles(first: Long, last: Long): Boolean {
        val networkRanges = calculateNetworkRanges(first, last)
        val (newCandles, successful) = loadFromNetwork(networkRanges)
        Log.i(TAG, "Loaded ${newCandles.size} candles from network")
        RoomDB.DB.insert(newCandles)
        if (successful) updateAvailableRanges(first, last)
        else Log.e(TAG, "Network load did not succeed.")

        return successful
    }

    suspend fun getRange(first: Long, last: Long): List<OHLCV> {
        loadNewCandles(first, last)
        return RoomDB.DB.getInRange(first, last)
    }

}