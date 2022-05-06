package com.remenyo.papertrader

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

object AnalyticsHelper {
    private const val TAG = "Analitics"
    private var lastReportedUid = ""


    fun reportException(e: Exception) = Firebase.crashlytics.recordException(e)

    fun reportLog(log: String) = Firebase.crashlytics.log(log)

    fun setUid(uid: String) {
        if (lastReportedUid != uid) {
            Firebase.analytics.setUserId(uid)
            Firebase.crashlytics.setUserId(uid)
            reportLog("UID: $uid")
            lastReportedUid = uid
        }
    }

    @SuppressLint("ComposableNaming")
    @Composable
    fun trackScreenView(name: String) {
        DisposableEffect(false) {
            Log.d(TAG, "screen enter : $name")
            Firebase.analytics.logEvent(
                FirebaseAnalytics.Event.SCREEN_VIEW, Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, name)
                }
            )
            onDispose { Log.d(TAG, "screen exit : $name") }
        }
    }
}