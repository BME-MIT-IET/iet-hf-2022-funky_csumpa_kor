package com.remenyo.papertrader

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.remenyo.papertrader.db.realtime.RealtimeDB
import com.remenyo.papertrader.db.realtime.RealtimeDBRepo
import com.remenyo.papertrader.db.room.CandleRepo

// https://proandroiddev.com/understanding-re-composition-in-jetpack-compose-with-a-case-study-9e7d96d98095

object SessionModel {
    const val TAG = "PaperTrader_SessionModel"

    // this object is two-way data binded to RealtimeDB
    // on init, we populate it from RealtimeDB
    // if it changes: it should be uploaded to RealtimeDB, but it does not have to be immediate, as only one user modifies it.
    var sessionData = SessionData()

    private var currentSessionID = ""
    private var lastSaveTimestamp = 0L

    private var isTimestampDirty = false // handle currentTimestamp differently
    private var isSessionDirty = false // is session data changed since last save / initial load

    // these are just views, only this module should change them (in sync with sessionData)
    var currentTimestamp by mutableStateOf<Long>(0)
    var currentMarketBuyPrice by mutableStateOf(0.0)
    var currentMarketSellPrice by mutableStateOf(0.0)
    var sessionDone by mutableStateOf(true)
    var currentCandle by mutableStateOf(OHLCV())


    var orders_view = mutableStateListOf<Order>()
    var candles_view = mutableListOf<OHLCV>()
    var pnl_view by mutableStateOf(0.0)
    var upnl_view by mutableStateOf(0.0)

    suspend fun saveSession(): Boolean {
        if (isSessionDirty) {
            Log.d(TAG, "Saving session")
            if (!RealtimeDBRepo.updateSession(sessionData, currentMarketSellPrice)) {
                Log.e(TAG, "saveSession updateSession")
                return false
            }
            isSessionDirty = false
        } else if (isTimestampDirty) {
            Log.d(TAG, "Updating session currentTS")
            if (!RealtimeDBRepo.updateSessionCurrentTS(sessionData)) {
                Log.e(TAG, "saveSession updateSessionCurrentTS")
                return false
            }
            isTimestampDirty = false
        }
        updateViews()
        return true
    }

    private fun clearSessionData() {
        sessionData = SessionData()
        currentSessionID = ""
        lastSaveTimestamp = System.currentTimeMillis()
        currentTimestamp = 0
        orders_view.clear()
        candles_view.clear()
        sessionDone = true
    }

    private fun updateViews() {
        currentTimestamp = sessionData.currentTS
        currentCandle = currentCandle()
        currentMarketBuyPrice = currentMarketBuyPrice()
        currentMarketSellPrice = currentMarketSellPrice()
        orders_view.clear()
        orders_view.addAll(sessionData.orders.values)
        sessionDone = sessionData.endTS <= sessionData.currentTS
        pnl_view = sessionData.pnl()
        upnl_view = sessionData.upnl(currentMarketSellPrice())
        /*sessionData.orders.values.forEach { master ->
            val index = orders_view.indexOfFirst { it.id == master.id }
            if (index == -1) orders_view.add(master)
            else {
                if (orders_view[index] != master)
                    orders_view[index] = master
                // Log.d(TAG, "Updating ${orders_view[index]} to $master")
                *//*  orders_view.removeAt(index)
                  orders_view.add(master)*//*
            }

        }*/
    }

    suspend fun init(sessionID: String): Boolean {
        Log.i(TAG, "Init $sessionID")
        if (currentSessionID != sessionID) {
            if (!saveSession()) {
                Log.e(TAG, "saveSession")
                return false
            }
            clearSessionData()
            sessionData = RealtimeDBRepo.fetchSession(sessionID)
            candles_view.addAll(CandleRepo.getRange(sessionData.startTS, sessionData.endTS))
            updateViews()
            Log.i(TAG, "Init done $sessionID")
        } else Log.d(TAG, "Session already loaded ($sessionID)")

        updateViews()
        currentSessionID = sessionID
        return true
    }

    /*fun dispose() {
        Log.i(TAG, "SessionModel dispose")
    }*/

    private fun currentCandle() =
        candles_view.find { it.ts > currentTimestamp } ?: candles_view.lastOrNull() ?: OHLCV()

    // todo close or low/high?

    private fun currentMarketSellPrice() = currentCandle.close

    private fun currentMarketBuyPrice() = currentCandle.close

    private fun priceCrossed(candle: OHLCV, price: Double) =
        candle.low < price && candle.high > price

    private fun addOrderToSessionData(o: Order) {
        isSessionDirty = true
        RealtimeDB.makeDocWithRandomID("/sessions/${sessionData.id}/orders")?.let {
            sessionData.orders.put(it, o.copy(id = it))
        }
    }

    private fun registerOrder(o: Order) {
        addOrderToSessionData(o)
        orders_view.add(o)
    }

    private fun orderModifier(id: String, fn: (Order) -> Order) {
        Log.d(TAG, "Modifying order $id: ${sessionData.orders[id]}")
        sessionData.orders[id]?.let { sessionData.orders[id] = fn(sessionData.orders[id]!!) }
        // orders_view.first { it.id == id }.let(fn)
        // https://stackoverflow.com/a/69718143
        val index = orders_view.indexOfFirst { it.id == id }
        orders_view[index] = fn(orders_view[index].copy())
        // updateViews()
    }

    /**
    NO CHECKS ARE ENFORCED
    ITS THE UI's job to post ONLY valid actions
     */
    object Command {
        fun addLimitOrderToSession(o: Order) {
            registerOrder(o)
        }

        fun addMarketOrderToSession(o: Order) {
            registerOrder(o.copy(openTS = currentTimestamp, bep = currentMarketBuyPrice()))
        }

        fun cancelOrder(orderID: String) {
            orderModifier(orderID) {
                return@orderModifier it.copy(cancelled = true)
            }
        }

        fun marketClose(orderID: String) {
            orderModifier(orderID) {
                return@orderModifier it.copy(
                    closeTS = currentTimestamp,
                    sep = currentMarketSellPrice()
                )
            }
        }

        fun liquidate() {
            sessionData.orders.values.forEach {
                if (!it.cancelled && !it.closed()) {
                    marketClose(it.id)
                }
            }
        }

        /*private fun trailSep(order: Order, candle: OHLCV) {
            // moved inside the advanceSession function, to avoid usage anywhere else
        }*/

        fun advanceSession(seconds: Long) {
            Log.i(TAG, "Advance by $seconds")
            val newTS = currentTimestamp + seconds
            candles_view.filter { it.ts in (currentTimestamp + 1)..newTS }.forEach { candle ->
                sessionData.orders.forEach { (id, order) ->
                    if (!order.closed() || !order.cancelled) {
                        if (!order.opened() && priceCrossed(candle, order.bep)) {
                            isSessionDirty = true
                            sessionData.orders[id] = order.copy(openTS = candle.ts)
                        } else if (order.opened() && !order.closed()) { // if order not filled yet
                            if (priceCrossed(candle, order.sep)
                            ) { // if market conditions allow fulfillment
                                isSessionDirty = true
                                sessionData.orders[id] = order.copy(closeTS = candle.ts)
                            } else if (order.trailing()) { // else trail the price if trailing is on
                                // START private fun trailSep(order: Order, candle: OHLCV)
                                isSessionDirty = true
                                // trail: new sep price movement depends on multiplier sign (+/-)
                                if (order.multi > 0 && candle.open < candle.close && sessionData.orders[id]?.sep!! < candle.close - order.trail) { // going up
                                    sessionData.orders[id] =
                                        order.copy(sep = candle.close - order.trail)
                                } else if (order.multi < 0 && candle.open > candle.close && sessionData.orders[id]?.sep!! > candle.close - order.trail) // going down
                                {
                                    sessionData.orders[id] =
                                        order.copy(sep = candle.close + order.trail)
                                }
                                // END private fun trailSep(order: Order, candle: OHLCV)
                            }
                        }
                    }
                }
            }
            currentTimestamp = newTS
            sessionData = sessionData.copy(currentTS = newTS)
            isTimestampDirty = true
            updateViews()

            if (sessionDone) {
                liquidate()
                updateViews()
            }

        }
    }
}