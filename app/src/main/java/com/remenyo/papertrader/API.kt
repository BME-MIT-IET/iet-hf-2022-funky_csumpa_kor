package com.remenyo.papertrader

import android.util.Log
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

// A few lines are from https://www.raywenderlich.com/6994782-android-networking-with-kotlin-tutorial-getting-started#toc-anchor-019
object API {
    private const val TAG = "API"

    private val ohlcvSerializer = ListSerializer(ListSerializer(Double.serializer()))
    private val availabilitySerializer = Boolean.serializer()

    interface CandleService {
        @GET("/cryptoapi/btc/data")
        suspend fun getCandles(
            @Query("start") startTS: Long,
            @Query("end") endTS: Long,
        ): ResponseBody

        @GET("/cryptoapi/btc/fully_available")
        suspend fun getAvailability(
            @Query("start") startTS: Long,
            @Query("end") endTS: Long
        ): ResponseBody
    }

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://logb.hu:8080/")
        .build()

    private val candleService: CandleService = retrofit.create(CandleService::class.java)

    suspend fun getAvailability(startTS: Long, endTS: Long): Boolean {
        Log.i(TAG, "Getting availability: $startTS - $endTS")
        try {
            AnalyticsHelper.reportLog("Getting availability: $startTS - $endTS")
            return Json.decodeFromString(
                availabilitySerializer, candleService.getAvailability(startTS, endTS).string()
            )
        } catch (e: Exception) {
            Log.e(TAG, e.toString(), e)
            AnalyticsHelper.reportException(e)
            throw e
        }
    }

    suspend fun loadCandles(startTS: Long, endTS: Long): List<OHLCV> {
        try {
            AnalyticsHelper.reportLog("Loading candles: $startTS - $endTS")
            return Json.decodeFromString(
                ohlcvSerializer,
                candleService.getCandles(startTS, endTS).string()
            ).map {
                if (it.size >= 6) OHLCV(
                    it[0].toLong(),
                    it[1],
                    it[2],
                    it[3],
                    it[4],
                    it[5]
                ) else OHLCV()
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString(), e)
            AnalyticsHelper.reportException(e)
            throw e
        }

        // this is way easier than retrofit, but whatever...
        // val txt = URL("http://rokakoma.duckdns.org:42069/cryptoapi/btc/data?start=$first&end=$last").readText()
    }
}