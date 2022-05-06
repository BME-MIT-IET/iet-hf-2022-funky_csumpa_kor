package com.remenyo.papertrader

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Stored in MMKV - CandleRepo uses this to calculate ranges
@Serializable
data class TSRange(@SerialName("a") val startTS: Long, @SerialName("b") val endTS: Long)

// Stored in Room
@Entity
data class OHLCV(
    @PrimaryKey val ts: Long = 0,
    val open: Double = 0.0,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val close: Double = 0.0,
    val volume: Double = 0.0
)

// Stored in RealtimeDB - Session data for session list in main menu
@IgnoreExtraProperties
@Serializable
data class UserSessionData(
    val id: String = "", val startTS: Long = 0, val endTS: Long = 0,

    var currentTS: Long = 0, // redundant
    val pnl: Double = 0.0 // redundant calculated
) {
    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "startTS" to startTS,
            "endTS" to endTS,
            "currentTS" to currentTS,
            "pnl" to pnl
        )
    }
}

// Stored in RealtimeDB - All session data
@IgnoreExtraProperties
data class SessionData(
    val id: String = "",
    val uid: String = "",
    val startTS: Long = 0,
    val endTS: Long = 0,
    val currentTS: Long = 0,
    val orders: MutableMap<String, Order> = HashMap(),

    val pnl: Double = 0.0, // calculated
    val upnl: Double = 0.0 // calculated
) {
    fun pnl(): Double {
        var sum = 0.0
        orders.values.forEach { sum += it.pnl() }
        return sum
    }

    fun upnl(currentMarketSellPrice: Double): Double {
        var sum = 0.0
        orders.values.forEach { sum += it.upnl(currentMarketSellPrice) }
        return sum
    }

    @Exclude
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "uid" to uid,
            "startTS" to startTS,
            "endTS" to endTS,
            "currentTS" to currentTS,
            "orders" to orders,
            "pnl" to pnl
        )
    }
}

// Stored in RealtimeDB - Part of session
@IgnoreExtraProperties
data class Order(
    val id: String = "",

    val cancelled: Boolean = false,

    val multi: Double = 1.0,
    val bep: Double = 0.0, // buy execution price: if not opened yet: at what price should the buy happen, if opened: at what price dit it open.
    val sep: Double = 0.0, // sell execution price: if not closed yet: at what price should the sell happen, if closed: at what price dit it close.

    val trail: Double = 0.0, // trailing value, if 0/null, it is not enabled

    val openTS: Long = 0,
    val closeTS: Long = 0
) {
    fun opened(): Boolean = openTS != 0.toLong()
    fun closed(): Boolean = closeTS != 0.toLong()
    fun trailing(): Boolean = trail != 0.0
    fun tradingScore(): Int =
        (if (opened() && !closed()) -3 else 0) + (if (!closed() && !cancelled) -2 else 0) + (if (!cancelled) -1 else 0)

    fun pnl(): Double = if (cancelled || !closed()) 0.0 else (sep - bep) * multi
    fun upnl(currentMarketSellPrice: Double) =
        if (cancelled || closed() || !opened()) 0.0 else (currentMarketSellPrice - bep) * multi
}