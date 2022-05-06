package com.remenyo.papertrader.db.room

import android.content.Context
import androidx.room.*
import com.remenyo.papertrader.OHLCV

object RoomDB {
    lateinit var DB: CandleDao

    fun init(context: Context) {
        DB = Room.databaseBuilder(context, AppDatabase::class.java, "CandleDB").build().dao()
    }

    @Database(entities = [OHLCV::class], version = 1)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun dao(): CandleDao
    }

    // No flows here: candle data should not change during
    @Dao
    interface CandleDao {
        @Query("SELECT * FROM OHLCV")
        suspend fun getAll(): List<OHLCV>

        @Query("SELECT * FROM OHLCV WHERE ts >= :first AND ts <= :last")
        suspend fun getInRange(first: Long, last: Long): List<OHLCV>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insert(candles: List<OHLCV>)
    }
}