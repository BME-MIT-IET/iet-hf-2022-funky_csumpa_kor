package com.remenyo.papertrader.ui.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.google.firebase.dynamiclinks.ktx.androidParameters
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.dynamiclinks.ktx.shortLinkAsync
import com.google.firebase.dynamiclinks.ktx.socialMetaTagParameters
import com.google.firebase.ktx.Firebase
import com.remenyo.papertrader.*
import com.remenyo.papertrader.AnalyticsHelper.trackScreenView
import com.remenyo.papertrader.ui.theme.AppTypography
import com.remenyo.papertrader.ui.theme.loss
import com.remenyo.papertrader.ui.theme.profit
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionInfo(navController: NavController<Screen>, id: String) {
    trackScreenView("session_info")

    var loading by remember { mutableStateOf(true) }
    val context = LocalContext.current

    val TAG = "PaperTrader_SessionInfo"

    LaunchedEffect(id) {
        SessionModel.init(id)
        loading = false
    }

    fun shareSession() {
        val dynamicLink = Firebase.dynamicLinks.shortLinkAsync {
            link =
                Uri.parse("https://www.traderapp-androidhf.web.app/newSession?start=${SessionModel.sessionData.startTS}&end=${SessionModel.sessionData.endTS}&uid=${Auth.uid}")
            domainUriPrefix = "https://papertrader.page.link"
            // Open links with this app on Android
            androidParameters { }
            // Open links with com.example.ios on iOS
            // iosParameters("com.example.ios") { }
            socialMetaTagParameters {
                title = "PaperTrader"
                description =
                    "Trade the past and practice together with your friends on PaperTrader!"
            }
        }

        AnalyticsHelper.reportLog("https://www.papertrader.web.app/newSession?start=${SessionModel.sessionData.startTS}&end=${SessionModel.sessionData.endTS}&uid=${Auth.uid}")

        dynamicLink.addOnCompleteListener {
            Log.i(TAG, "New page link created: ${it.result.shortLink} ${it.result.previewLink}")
            val share = Intent.createChooser(Intent().apply {
                type = "text/plain"
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, it.result.shortLink.toString())

                // (Optional) Here we're setting the title of the content
                putExtra(
                    Intent.EXTRA_TITLE,
                    "Trade the past and practice together with your friends on PaperTrader!"
                )

                // (Optional) Here we're passing a content URI to an image to be displayed
                // flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            }, null)
            startActivity(context, share, null)
        }.addOnFailureListener {
            AnalyticsHelper.reportException(it)
            // todo toast error
            Log.e(TAG, it.message, it)
        }
    }


    Scaffold(topBar = {
        TopAppBar(
            "Session info",
            leftButton = { BackButton() { navController.pop() } }, rightButton = {
                IconButton(onClick = { shareSession() }) {
                    Icon(
                        Icons.Default.Share, "Share session"
                    )
                }
            })
    }) { paddingValues ->
        if (loading) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                if (SessionModel.sessionData.startTS != 0.toLong() && SessionModel.sessionData.endTS != 0.toLong())
                    Text(
                        dateTimeText(
                            SessionModel.sessionData.startTS,
                            SessionModel.sessionData.endTS
                        ),
                        style = AppTypography.headlineLarge
                    )
                else Text("Invalid date", style = AppTypography.headlineLarge)
                Text(
                    "Progress: ${
                        completenessPercentText(
                            SessionModel.sessionData.startTS,
                            SessionModel.sessionData.endTS,
                            SessionModel.currentTimestamp
                        )
                    }", style = AppTypography.headlineMedium
                )
                Spacer(Modifier.height(8.dp))

                if (!SessionModel.sessionDone) {
                    Text(
                        "UPnL: %.2f".format(SessionModel.upnl_view),
                        style = AppTypography.headlineMedium,
                        color = if (SessionModel.upnl_view < 0) loss else profit
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth().semantics { testTag="trade" },
                        onClick = { navController.navigate(Screen.SessionPlay(SessionModel.sessionData.id)) }) {
                        Text(if (SessionModel.currentTimestamp < SessionModel.sessionData.endTS) "Trade" else "View chart")
                    }
                } else {
                    Text(
                        "PnL: %.2f".format(SessionModel.pnl_view),
                        style = AppTypography.headlineMedium,
                        color = if (SessionModel.pnl_view < 0) loss else profit
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier.height(230.dp)
                    ) {
                        Chart()
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Orders", style = AppTypography.headlineLarge)
                Spacer(Modifier.height(16.dp))
                if (SessionModel.orders_view.isNotEmpty()) {
                    LazyColumn {
                        items(SessionModel.orders_view.sortedBy { it.closeTS }.reversed()) {
                            OrderCard(it)
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                } else {
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("No orders yet", style = AppTypography.headlineSmall)
                    }
                }
            }
        }
    }
}