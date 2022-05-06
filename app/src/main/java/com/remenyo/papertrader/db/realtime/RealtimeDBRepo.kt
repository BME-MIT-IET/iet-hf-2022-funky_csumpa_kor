package com.remenyo.papertrader.db.realtime

import android.util.Log
import com.remenyo.papertrader.Auth
import com.remenyo.papertrader.SessionData
import com.remenyo.papertrader.UserSessionData
import com.remenyo.papertrader.db.room.CandleRepo

object RealtimeDBRepo {

    private const val TAG = "PaperTrader_SessionMan"

    // GETTERS

    suspend fun fetchUserSessions(): List<UserSessionData> {
        Auth.ensureUser()
        return RealtimeDB.fetchList("/users/${Auth.uid}/sessions")
    }

    suspend fun fetchSession(id: String): SessionData {
        return RealtimeDB.fetchDocument("/sessions/$id")
    }

    // SETTERS

    suspend fun createSession(
        startTS: Long,
        endTS: Long,
        currentTS: Long
    ): Pair<String, Boolean> {
        // 1. Get candle data from network, save into RoomDB
        // 2. Generate SessionData
        // 3. Upload session to RealtimeDB /sessions
        // 4. Make User link SessionUserData
        // 5. Upload session to RealtimeDB /user/${uid}

        // 1. Get candle data from network, save into RoomDB
        if (!CandleRepo.loadNewCandles(startTS, endTS)) {
            Log.e(TAG, "loadNewCandles")
            return Pair("", false)
        }

        if (!Auth.ensureUser()) // we need a user
        {
            Log.e(TAG, "ensureUser")
            return Pair("", false)
        }

        // 2. Generate SessionData
        val sessionID = RealtimeDB.makeDocWithRandomID("/sessions")

        if (sessionID == null) {
            Log.e(TAG, "makeDocWithrandomID \"/sessions\"")
            return Pair("", false)
        }

        val sessionData = SessionData(sessionID, Auth.uid, startTS, endTS, currentTS)

        // 3. Upload session to RealtimeDB /sessions
        if (!RealtimeDB.updateDoc("/sessions/$sessionID", sessionData.toMap())) {
            Log.e(TAG, "updateDoc \"/sessions/$sessionID\"")
            return Pair("", false)
        }

        // 4. Make User link SessionUserData
        val userSessionData = UserSessionData(sessionID, startTS, endTS, currentTS)

        // 5. Upload session to RealtimeDB /user/${uid}
        if (!RealtimeDB.setValue(
                "users/${Auth.uid}/sessions/$sessionID", userSessionData
            )
        ) {
            Log.e(TAG, "setDoc(users/${Auth.uid}/sessions/$sessionID\"")
            return Pair("", false)
        }

        return Pair(sessionID, true)
    }

    suspend fun updateSession(sessionData: SessionData, currentMarketSellPrice: Double): Boolean {
        val session = sessionData.copy(
            pnl = sessionData.pnl(),
            upnl = sessionData.upnl(currentMarketSellPrice)
        )

        // 3. Update session to RealtimeDB /sessions
        if (!RealtimeDB.updateDoc("/sessions/${session.id}", session.toMap())) {
            Log.e(TAG, "updateSession updateDoc \"/sessions/${session.id}\"")
            return false
        }

        // 5. Update session to RealtimeDB /user/${uid}
        if (!RealtimeDB.updateDoc(
                "/users/${session.uid}/sessions/${session.id}",
                UserSessionData(
                    session.id,
                    session.startTS,
                    session.endTS,
                    session.currentTS,
                    session.pnl
                ).toMap()
            )
        ) {
            Log.e(
                TAG,
                "updateSession updateDoc(\"/users/${session.uid}/sessions/${session.id}\""
            )
            return false
        }

        return true
    }

    suspend fun updateSessionCurrentTS(sessionData: SessionData): Boolean {
        if (!RealtimeDB.updateDoc(
                "/sessions/${sessionData.id}", mapOf("currentTS" to sessionData.currentTS)
            )
        ) {
            Log.e(TAG, "updateSessionCurrentTS updateDoc(\"/sessions/${sessionData.id}\"")
            return false
        }

        if (!RealtimeDB.updateDoc(
                "/users/${sessionData.uid}/sessions/${sessionData.id}",
                mapOf("currentTS" to sessionData.currentTS)
            )
        ) {
            Log.e(
                TAG,
                "updateSessionCurrentTS updateDoc(\"/users/${sessionData.uid}/sessions/${sessionData.id}\""
            )
            return false
        }
        return true
    }

    suspend fun detachUserSessions(session_ids: List<String>) {
        session_ids.forEach { id ->
            RealtimeDB.setValue("/sessions/$id/detachedFromUser", true)
        }
    }

    suspend fun attatchUserSessions(session_ids: List<String>) {
        session_ids.forEach { id ->
            RealtimeDB.deleteDoc("/sessions/$id/detachedFromUser")
        }
    }

    suspend fun updateUserFCMToken(token: String) {
        Log.d(TAG, "Setting FCM token to: $token")
        RealtimeDB.setValue("/users/${Auth.uid}/fcm_token", token)
    }

    suspend fun updateLastLoginTs() {
        val ts = System.currentTimeMillis() / 1000
        Log.d(TAG, "Updating last login ts: $ts")
        RealtimeDB.setValue("/users/${Auth.uid}/last_login", ts)
    }

    // DELETE
    suspend fun deleteUserSessions(sessionList: List<String>): Boolean {
        sessionList.forEach {
            if (!RealtimeDB.deleteDoc("/sessions/${it}")) {
                return false
            }
        }
        return true
    }

    suspend fun deleteUser(uid: String): Boolean {
        return RealtimeDB.deleteDoc("/users/$uid")
    }

    suspend fun deleteUserWithSessions(uid: String) {
        val sessions = fetchUserSessions().map { it.id }
        detachUserSessions(sessions)
        deleteUserSessions(sessions)
        deleteUser(uid)
    }

}