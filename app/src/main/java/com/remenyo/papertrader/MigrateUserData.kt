package com.remenyo.papertrader

import android.util.Log
import com.remenyo.papertrader.db.PreferenceKeys
import com.remenyo.papertrader.db.realtime.RealtimeDB
import com.remenyo.papertrader.db.realtime.RealtimeDBRepo
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

suspend fun MigrateUserData(loginEmail: String, emailLink: String): Boolean {
    val TAG = "PaperTrader_MigrateUser"

    // 0. logged in with anon uid
    // 1. save anon sessions locally
    // 2. save anon uid

    // 2.1 tag anon user sessions in /sessions as transferable

    // 3. delete anon user data
    // 4. delete anon user from auth

    // 5. log in with email
    // 5.1 if login failed, make new anon user, to get uid

    // foreach saved anon session from 1.
    // 6. put back sessions to the currently logged in user
    // 7. update sessions/sessionid/uid to uid
    // 8. remove transferable tag from migrated entries


    // 0. logged in with anon uid
    if (!Auth.signedIn) {
        Log.e(TAG, "No user logged in.")
        return false
    }

    // 1. save anon sessions locally
    val anonSessions = RealtimeDBRepo.fetchUserSessions()
    App.KVStore.encode(
        PreferenceKeys.anonUserData_string,
        Json.encodeToString(ListSerializer(UserSessionData.serializer()), anonSessions)
    )

    // 2. save anon uid
    val anonUID = Auth.uid

    // 2.1 tag anon user sessions in /sessions as transferable
    RealtimeDBRepo.detachUserSessions(anonSessions.map { it.id })

    // 3. delete anon user data
    if (!RealtimeDBRepo.deleteUser(anonUID)) {
        Log.e(TAG, "RealtimeDBRepo.deleteUser(anonUID)")
        return false
    }

    // 4. delete anon user from auth
    Auth.deleteAccount()

    // 5. log in with email
    if (!Auth.loginWithEmailLink(loginEmail, emailLink)) {
        Log.e(TAG, "loginWithEmailLink")
        return false
    }

    if (!recoverSavedData()) {
        Log.e(TAG, "recoverSavedData")
        return false
    }

    Log.i(TAG, "Successfully migrated user data")
    return true
}

suspend fun recoverSavedData(): Boolean {
    val TAG = "PaperTrader_RecoverSavedData"

    // 5.1 if login failed, make new anon user
    if (!Auth.ensureUser()) {
        Log.e(TAG, "ensureUser")
        return false
    }

    val anonSessions = Json.decodeFromString(
        ListSerializer(UserSessionData.serializer()),
        App.KVStore.decodeString(PreferenceKeys.anonUserData_string, "[]") ?: "[]"
    )

    // foreach saved session
    for (session in anonSessions) {
        // 6. put back sessions to the currently logged in user
        if (!RealtimeDB.setValue("/users/${Auth.uid}/sessions/${session.id}", session)) {
            Log.e(TAG, "6. put back sessions to the currently logged in user")
            return false
        }

        // 7. update sessions/sessionid/userid to uid
        var s = RealtimeDBRepo.fetchSession(session.id)
        Log.d(TAG, "Session: $s")
        s = s.copy(uid = Auth.uid)
        Log.d(TAG, "Session: $s")

        if (!RealtimeDB.updateDoc("/sessions/${session.id}/", mapOf("uid" to Auth.uid))
        ) {
            Log.e(TAG, "update sessions/sessionid/userid to uid")
            return false
        }
    }

    // 8. remove transferable tag from migrated entries
    RealtimeDBRepo.attatchUserSessions(anonSessions.map { it.id })

    // on success we dont need it
    App.KVStore.remove(PreferenceKeys.anonUserData_string)

    return true
}