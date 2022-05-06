package com.remenyo.papertrader

import com.google.firebase.messaging.FirebaseMessagingService
import com.remenyo.papertrader.db.realtime.RealtimeDBRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class FirebaseMessaging : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.Main).launch {
            while (!Auth.signedIn) {
                delay(1000)
            }
            RealtimeDBRepo.updateUserFCMToken(token)
        }
    }
}