package com.remenyo.papertrader.db.realtime

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.remenyo.papertrader.AnalyticsHelper
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object RealtimeDB {
    const val TAG = "PaperTrader_RealtimeDB"
    val database = Firebase.database

    init {
        database.setPersistenceEnabled(true)
        database.setPersistenceCacheSizeBytes(100 * 1_000_000)
    }

    suspend inline fun <reified T> fetchDocument(path: String): T =
        suspendCoroutine { cont ->
            val documentRef = database.getReference(path)
            var document: T = T::class.java.newInstance()

            documentRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        document = snapshot.getValue(T::class.java)!!
                    } catch (e: Exception) {
                        AnalyticsHelper.reportException(e)
                        Log.e(TAG, e.toString())
                    }
                    cont.resume(document)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "cancel request: $error")
                    cont.resume(document)
                }
            })
        }

    // why have boilerplate code when you can throw 5 modifiers in one function definition
    // src for reified: https://stackoverflow.com/a/46870546
    suspend inline fun <reified T> fetchList(path: String): List<T> =
        suspendCoroutine { cont ->
            val documentsRef = database.reference.child(path)
            val documents = mutableListOf<T>()

            documentsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d(TAG, "Loaded ${snapshot.children.count()} entries.")
                    try {
                        snapshot.children.mapTo(documents) {
                            // Log.d(TAG, "child: ${it.getValue(UserSessionData::class.java)}")
                            it.getValue(T::class.java)!!
                        }
                    } catch (e: Exception) {
                        AnalyticsHelper.reportException(e)
                        Log.e(TAG, e.toString())
                    }
                    cont.resume(documents)
                }

                override fun onCancelled(error: DatabaseError) {
                    AnalyticsHelper.reportException(error.toException())
                    Log.e(TAG, "updateUserSessions: $error")
                    cont.resume(documents)
                }
            })
        }

    fun makeDocWithRandomID(path: String): String? {
        return database.getReference(path).push().key
    }

    suspend fun setValue(path: String, document: Any): Boolean =
        suspendCoroutine { cont ->
            database.reference.child(path).setValue(document)
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener {
                    Log.e(TAG, it.toString())
                    cont.resume(false)
                }
        }

    suspend fun updateDoc(path: String, document: Map<String, Any>): Boolean =
        suspendCoroutine { cont ->
            database.getReference(path).updateChildren(document)
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener {
                    Log.e(TAG, it.toString())
                    cont.resume(false)
                }
        }

    suspend fun deleteDoc(path: String): Boolean =
        suspendCoroutine { cont ->
            database.getReference(path).removeValue()
                .addOnSuccessListener { cont.resume(true) }
                .addOnFailureListener {
                    Log.e(TAG, it.toString(), it.cause)
                    cont.resume(false)
                }
        }
}