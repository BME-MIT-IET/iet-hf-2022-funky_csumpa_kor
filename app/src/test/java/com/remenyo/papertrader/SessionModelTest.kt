package com.remenyo.papertrader

import android.os.PerformanceHintManager
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.remenyo.papertrader.db.realtime.RealtimeDB
import com.remenyo.papertrader.db.realtime.RealtimeDBRepo
import com.remenyo.papertrader.db.room.CandleRepo
import io.mockk.*
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.*
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*

class SessionModelTest {
    val candles: List<OHLCV> = listOf<OHLCV>(
        OHLCV(0,0.0,1.0,0.0,1.0,1.0),
        OHLCV(1,1.0,2.0,0.0,1.0,1.0),
        OHLCV(2,1.0,3.0,1.0,3.0,1.0),
        OHLCV(3,3.0,6.0,3.0,6.0,1.0),
        OHLCV(4,7.0,9.0,7.0,9.0,1.0),
        OHLCV(5,7.0,7.0,2.0,3.0,1.0),
        OHLCV(6,5.0,6.0,3.0,4.0,1.0),
        OHLCV(7,4.0,5.0,1.0,3.0,1.0),
        OHLCV(8,3.0,6.0,3.0,6.0,1.0),
        OHLCV(9,7.0,9.0,7.0,9.0,1.0),
        OHLCV(10,7.0,7.0,2.0,3.0,1.0)
    )

    var sd= SessionData(
        startTS = 0,
        endTS = 10,
        currentTS = 0,
        id = "test",
        orders = mutableMapOf<String,Order>("first" to Order(cancelled = false, id = "OrderToCancel")),
    )

    @Before
    fun initialization(){
        SessionModel.sessionData=SessionData(

        )
        SessionModel.orders_view = SnapshotStateList()
    }

    @Test
    fun isTimeStampDirtyTest() = runBlocking{
        mockkObject(SessionModel)
        mockkObject(RealtimeDBRepo)
        mockkObject(CandleRepo)

        coEvery {
            RealtimeDBRepo.fetchSession(sd.id)
        } returns sd

        coEvery {
            CandleRepo.getRange(sd.startTS,sd.endTS)
        } returns candles

        coEvery {
            RealtimeDBRepo.updateSessionCurrentTS(any())
        }returns true

        SessionModel.Command.advanceSession(1)

        SessionModel.saveSession()

        assertEquals(SessionModel.currentTimestamp,1)

        coVerify { RealtimeDBRepo.updateSessionCurrentTS(SessionData(currentTS = 1)) }

        unmockkAll()
    }

    @Test
    fun isSessionDirtyTest() = runBlocking {
        mockkObject(SessionModel)
        mockkObject(RealtimeDBRepo)
        mockkObject(CandleRepo)

        mockkStatic(FirebaseDatabase::class)
        every { FirebaseDatabase.getInstance() } returns mockk(relaxed = true)

        mockkObject(RealtimeDB)

        var order = Order(
            id="testOrder",
            sep = 0.0,
            multi = 1.0,
            trail = 0.0
        )

        coEvery {
            RealtimeDBRepo.fetchSession(sd.id)
        } returns sd

        coEvery {
            CandleRepo.getRange(sd.startTS,sd.endTS)
        } returns candles

        coEvery {
            RealtimeDBRepo.updateSession(any(),any())
        }returns true

        coEvery {
            RealtimeDB.makeDocWithRandomID(any())
        } returns null

        SessionModel.Command.addMarketOrderToSession(order)

        SessionModel.saveSession()

        coVerify { RealtimeDBRepo.updateSession(SessionData(currentTS = 0),0.0) }

        unmockkAll()

    }


    @Test
    fun isOrderCancelled()= runBlocking {
        mockkObject(SessionModel)
        mockkObject(RealtimeDBRepo)
        mockkObject(CandleRepo)

        mockkStatic(FirebaseDatabase::class)
        every { FirebaseDatabase.getInstance() } returns mockk(relaxed = true)

        mockkObject(RealtimeDB)

        coEvery {
            RealtimeDBRepo.fetchSession(sd.id)
        } returns sd

        coEvery {
            CandleRepo.getRange(sd.startTS,sd.endTS)
        } returns candles

        coEvery {
            RealtimeDBRepo.updateSession(any(),any())
        }returns true

        coEvery {
            RealtimeDB.makeDocWithRandomID(any())
        } returns null

        SessionModel.sessionData.orders.put("first", Order(cancelled = false, id = "OrderToCancel"))
        SessionModel.orders_view.add(Order(cancelled = false, id = "first"))

        SessionModel.Command.cancelOrder("first")

        assertEquals(true, SessionModel.sessionData.orders["first"]!!.cancelled)

        unmockkAll()
    }

    @Test
    fun isMarketClosed() = runBlocking {
        mockkObject(SessionModel)
        mockkObject(RealtimeDBRepo)
        mockkObject(CandleRepo)

        mockkStatic(FirebaseDatabase::class)
        every { FirebaseDatabase.getInstance() } returns mockk(relaxed = true)

        mockkObject(RealtimeDB)

        coEvery {
            RealtimeDBRepo.fetchSession(sd.id)
        } returns sd

        coEvery {
            CandleRepo.getRange(sd.startTS,sd.endTS)
        } returns candles

        coEvery {
            RealtimeDBRepo.updateSession(any(),any())
        }returns true

        coEvery {
            RealtimeDB.makeDocWithRandomID(any())
        } returns null

        SessionModel.sessionData.orders.put("first", Order(cancelled = false, id = "OrderToCancel", sep = 2.0))
        SessionModel.orders_view.add(Order(cancelled = false, id = "first"))

        SessionModel.Command.marketClose("first")

        assertEquals(0, SessionModel.sessionData.orders["first"]!!.closeTS)
        assertEquals(0.0,SessionModel.sessionData.orders["first"]!!.sep)
    }
}