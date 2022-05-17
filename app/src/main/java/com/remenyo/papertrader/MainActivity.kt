package com.remenyo.papertrader

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.material3.ExperimentalMaterial3Api
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.remenyo.papertrader.db.realtime.RealtimeDBRepo
import com.remenyo.papertrader.db.room.RoomDB
import com.remenyo.papertrader.ui.components.*
import com.remenyo.papertrader.ui.components.trading.Trading
import com.remenyo.papertrader.ui.theme.AppTheme
import com.tencent.mmkv.MMKV
import dev.olshevski.navigation.reimagined.AnimatedNavHost
import dev.olshevski.navigation.reimagined.NavAction
import dev.olshevski.navigation.reimagined.NavBackHandler
import dev.olshevski.navigation.reimagined.rememberNavController
import kotlinx.coroutines.*
import kotlinx.parcelize.Parcelize
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


sealed class Screen : Parcelable {

    @Parcelize
    object Login : Screen()

    @Parcelize
    object Home : Screen()

    @Parcelize
    data class NewSession(val start: Long? = null, val end: Long? = null) : Screen()

    @Parcelize
    data class SessionInfo(val id: String) : Screen()

    @Parcelize
    data class SessionPlay(val id: String) : Screen()

    @Parcelize
    object Settings : Screen()
}

class App : ComponentActivity() {


    companion object {
        lateinit var KVStore: MMKV
        private const val TAG = "PaperTrader_Main"
        var offset:Int = 0
    }

    override fun onResume() {
        val calendar = Calendar.getInstance(Locale.getDefault());
        val offset = -(calendar.get(Calendar.ZONE_OFFSET) + calendar.get(Calendar.DST_OFFSET)) / 1000
        App.offset=offset
        super.onResume()
    }

    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /*setContent {
            AppTheme {
                Scaffold {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("PaperTrader", style = AppTypography.headlineLarge)
                    }
                }
            }
        }*/

        val context = this.applicationContext

        val initialBackStack = mutableListOf<Screen>(Screen.Home)

        suspend fun processDynamicLink(link: String) {
            Log.d(TAG, "Deep link: $link")

            // Passwordless login handler + user sign in ensurer
            Auth.signIn(link)

            if (link.contains("papertrader.web.app")) {
                val params = mutableMapOf<String, String>()
                val afterDomain = link.split("/").last().split("?")
                afterDomain.last().split("&").forEach {
                    val p = it.split("=")
                    if (p.size > 1) {
                        params[p[0]] = p[1]
                    }
                }

                if (afterDomain[0] == "newSession" && params["start"]?.toLongOrNull() != null && params["end"]?.toLongOrNull() != null) {
                    initialBackStack.add(
                        Screen.NewSession(
                            params["start"]!!.toLong(), params["end"]!!.toLong()
                        )
                    )
                }
            }
        }

        // Dynamic link handler
        suspend fun getDynamicLink() = suspendCoroutine<String?> { cont ->
            Firebase.dynamicLinks.getDynamicLink(intent)
                .addOnSuccessListener { pendingDynamicLinkData ->
                    // Get deep link from result (may be null if no link is found)
                    if (pendingDynamicLinkData != null) cont.resume(pendingDynamicLinkData.link.toString())
                    else cont.resume(null)

                }.addOnFailureListener { e ->
                    Log.w(TAG, "getDynamicLink:onFailure", e)
                    cont.resume(null)
                }
        }

        CoroutineScope(Dispatchers.Main).launch {
            // these are probably not needed
            Firebase.analytics.setAnalyticsCollectionEnabled(true)
            Firebase.crashlytics.setCrashlyticsCollectionEnabled(true)

            // i know these are independent of each other, so i can call them at the same time
            awaitAll(async {
                // Interprets actions if there are any in the intent
                getDynamicLink()?.let {
                    processDynamicLink(it)
                }
            }, async {
                // SharedPreferences, but better (by Tencent)
                MMKV.initialize(context)
                KVStore = MMKV.mmkvWithID("App")
            }, async {
                // Init my little chart binary string
                ChartBinary.init(context)
            }, async {
                // Candle database init
                RoomDB.init(context)
            })

            val firebaseAppCheck = FirebaseAppCheck.getInstance()
            firebaseAppCheck.installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance()
            )

            recoverSavedData()


            // todo until signinflow + dynamic links is not done, show loading screen
            setContent {
                val navController = rememberNavController(
                    initialBackStack
                    // todo welcome screen on first start
                )

                NavBackHandler(navController)
                AppTheme {
                    AnimatedNavHost(navController, transitionSpec = { action, _, _ ->
                        val direction = if (action == NavAction.Pop) {
                            AnimatedContentScope.SlideDirection.Down
                        } else {
                            AnimatedContentScope.SlideDirection.Up
                        }
                        slideIntoContainer(direction) with slideOutOfContainer(direction)
                    }) { screen ->
                        when (screen) {
                            is Screen.Login -> Login(navController)
                            is Screen.Home -> Home(navController)
                            is Screen.NewSession -> NewSession(
                                navController, screen.start, screen.end
                            )
                            is Screen.SessionInfo -> SessionInfo(
                                navController, screen.id
                            )
                            is Screen.SessionPlay -> Trading(
                                navController, screen.id
                            )
                            is Screen.Settings -> Settings(navController)
                        }
                    }
                }
            }

            // After screen show

            this.launch {
                RealtimeDBRepo.updateLastLoginTs()
                Firebase.messaging.token.addOnSuccessListener {

                }.addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d(TAG, "FCM completed successfully, result: ${it.result}")
                        CoroutineScope(Dispatchers.Main).launch {
                            RealtimeDBRepo.updateUserFCMToken(it.result)
                        }
                    } else Log.e(TAG, "FCM error happened: ${it.exception}", it.exception)
                }
            }

        }
    }
}

