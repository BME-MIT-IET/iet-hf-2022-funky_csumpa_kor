package com.remenyo.papertrader.db

// These keys are used to set/get data from a SharedPreferences provider like MMKV
object PreferenceKeys {

    // values which have defaults, are shown on settings menu

    // Auth
    const val emailToLoginWith_string = "emailToLoginWith"

    // CandleRepo
    const val availableRangeList_string = "availableRangeList"

    // Migration
    const val anonUserData_string = "anonUserData"

    // SessionModel
    const val autoSaveIntervalSec_long = "autoSaveInterval"
    const val autoSaveIntervalSec_long_default: Long = 10

    // TradingScreen
    const val refreshIntervalMs_long = "refreshIntervalMs"
    const val refreshIntervalMs_long_default: Long = 1000

    const val speedUpMultiplier_long = "speedUpMultiplier_long"
    const val speedUpMultiplier_long_default: Long = 60

    const val maxMultiplier_int = "maxMultiplier_int"
    const val maxMultiplier_int_default: Int = 5
}