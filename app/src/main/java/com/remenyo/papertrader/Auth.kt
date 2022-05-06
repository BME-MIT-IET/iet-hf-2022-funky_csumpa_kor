package com.remenyo.papertrader

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.ktx.actionCodeSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.remenyo.papertrader.db.PreferenceKeys
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
Firebase Auth instance with observable user data + auth function helpers
 */
object Auth {
    private const val TAG = "PaperTrader_Auth"
    private val auth: FirebaseAuth = Firebase.auth

    // Firebase
    var signedIn by mutableStateOf(false)
    var isUserAnon by mutableStateOf(false)
    var userEmail by mutableStateOf("")
    var uid by mutableStateOf("")

    private fun updateObservables() {
        val user = auth.currentUser
        if (user == null) {
            signedIn = false
            userEmail = ""
            isUserAnon = true
            uid = ""
        } else {
            signedIn = true
            userEmail = user.email ?: ""
            isUserAnon = user.isAnonymous
            uid = user.uid
            AnalyticsHelper.setUid(uid)
        }
    }

    init {
        Log.d(TAG, "Init, login status: ${auth.currentUser != null}")
        // This does not work
        FirebaseAuth.AuthStateListener {
            Log.i(TAG, "Auth state changed")
            it.uid?.let { uid ->
                AnalyticsHelper.reportLog("Uid: $uid")
            }
            updateObservables()
        }
        updateObservables()

        // This should be used
        /*// There are limited circumstances under which GlobalScope can be legitimately and safely used,
        // such as top-level background processes that must stay active for the whole duration of the
        // application's lifetime. Because of that, any use of GlobalScope requires an explicit opt-in
        @OptIn(DelicateCoroutinesApi::class)
        GlobalScope.launch {
            while (true) {
                delay(1000)
                updateObservables(getCurrentUser())
            }
        }*/

    }

    private suspend fun tryLinkAnonWithEmail(loginEmail: String, emailLink: String): Boolean =
        suspendCoroutine { cont ->
            val credential = EmailAuthProvider.getCredentialWithLink(loginEmail, emailLink)

            Firebase.auth.currentUser?.linkWithCredential(credential)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Successfully linked emailLink credential!")
                        cont.resume(true)
                        // val result = task.result
                        // You can access the new user via result.getUser()
                        // Additional user info profile *not* available via:
                        // result.getAdditionalUserInfo().getProfile() == null
                        // You can check if the user is new or existing:
                        // result.getAdditionalUserInfo().isNewUser()
                    } else {
                        Log.e(
                            TAG, "Error linking emailLink credential", task.exception
                        )
                        cont.resume(false)
                        // This nasty error happens when the user started an anonymous session but already has a registered account.
                        // The linking process fails, as the email is already connected to a UID.
                        // This case needs a fairly complex migration process: MigrateUserData()
                    }
                }
        }

    suspend fun signOut() {
        auth.signOut()
        ensureUser()
    }

    fun deleteAccount() {
        auth.currentUser?.let {
            try {
                it.delete()
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                CoroutineScope(Dispatchers.IO).launch {
                    it.email?.let { it1 -> startEmailLogin(it1) }
                    // todo toast to tell the user login then can delete
                }
            }
        }
    }

    suspend fun anonymousLogin(): Boolean = suspendCoroutine { cont ->
        auth.signInAnonymously().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(
                    TAG, "signInAnonymously:success"
                )
                cont.resume(true)
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInAnonymously:failure", task.exception)
                task.exception?.let { AnalyticsHelper.reportException(it) }
                cont.resume(false)
            }
        }
    }

    suspend fun startEmailLogin(email: String): Boolean =
        // src: https://stackoverflow.com/a/48562175
        suspendCoroutine { cont ->

            App.KVStore.encode(PreferenceKeys.emailToLoginWith_string, email)

            Log.d(
                TAG,
                "Saved email for later login: ${App.KVStore.decodeString(PreferenceKeys.emailToLoginWith_string)}"
            )

            // todo if anon, and auto link does not work, move sessions, and session owner from anon to new, then delete anon

            val actionCodeSettings = actionCodeSettings {
                // URL you want to redirect back to. The domain (www.example.com) for this
                // URL must be whitelisted in the Firebase Console.
                url = "https://traderapp-androidhf.web.app/finishSignUp"
                // This must be true
                handleCodeInApp = true
                // setIOSBundleId("com.remenyo.papertrader")
                setAndroidPackageName(
                    "com.remenyo.papertrader", true, /* installIfNotAvailable */
                    null // "1" /* minimumVersion */
                )
            }

            auth.sendSignInLinkToEmail(email, actionCodeSettings).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    cont.resume(true)
                    Log.d(TAG, "Email sent.")
                } else {
                    Log.e(TAG, "Login email error", task.exception)
                    cont.resume(false)
                }
            }
        }

    suspend fun loginWithEmailLink(loginEmail: String, emailLink: String): Boolean =
        suspendCoroutine { cont ->
            auth.signInWithEmailLink(loginEmail, emailLink).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i(
                        TAG, "Successfully signed in with email link! (${loginEmail})"
                    )
                    cont.resume(true)
                    // val result = task.result
                    // You can access the new user via result.getUser()
                    // Additional user info profile *not* available via:
                    // result.getAdditionalUserInfo().getProfile() == null
                    // You can check if the user is new or existing:
                    // result.getAdditionalUserInfo().isNewUser()
                } else {
                    Log.e(
                        TAG, "Error signing in with email link", task.exception
                    )
                    cont.resume(false)
                }
            }
        }

    /**
     * Tries to sign in with email
     * */
    suspend fun signIn(emailLink: String) {
        Log.d(TAG, "Signin emailLink check: $emailLink")
        if (auth.isSignInWithEmailLink(emailLink)) {
            Log.i(TAG, "Email link detected: $emailLink")

            App.KVStore.decodeString(PreferenceKeys.emailToLoginWith_string)?.let { loginEmail ->
                if (isUserAnon && !tryLinkAnonWithEmail(loginEmail, emailLink)) MigrateUserData(
                    loginEmail, emailLink
                )
                else loginWithEmailLink(loginEmail, emailLink)
            }
        }
        ensureUser()
    }

    /**
     * Logs in with anon, if not logged in already
     * */
    suspend fun ensureUser(): Boolean {
        updateObservables()
        return if (signedIn) true
        else {
            val res = anonymousLogin()
            updateObservables()
            res
        }
    }
}