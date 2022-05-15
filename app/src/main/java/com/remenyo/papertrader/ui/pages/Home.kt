package com.remenyo.papertrader.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.remenyo.papertrader.*
import com.remenyo.papertrader.AnalyticsHelper.trackScreenView
import com.remenyo.papertrader.db.realtime.RealtimeDBRepo
import com.remenyo.papertrader.ui.theme.AppTypography
import com.remenyo.papertrader.ui.theme.RoundShapes
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.navigate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(navController: NavController<Screen>) {
    trackScreenView("home")

    var userSessions by remember { mutableStateOf(emptyList<UserSessionData>()) }
    val loading = rememberSwipeRefreshState(false)
    val context = LocalContext.current

    suspend fun updateSessionList() {
        loading.isRefreshing = true
        userSessions = RealtimeDBRepo.fetchUserSessions().reversed()
        delay(200) // It's so fast that I need a delay to show "its doing something"
        loading.isRefreshing = false
    }

    LaunchedEffect(Auth.uid) {
        updateSessionList()
    }

    Scaffold(topBar = {
        TopAppBar("PaperTrader", leftButton = {
            Row(Modifier.padding(8.dp)) {
                IconButton(onClick = { navController.navigate(Screen.Settings) }) {
                    Icon(Icons.Default.Settings, "Settings")
                }
                IconButton(onClick = {
                    val selectorIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:") // only email apps should handle this
                    }
                    val emailIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("remenyo@proton.me"))
                        putExtra(
                            Intent.EXTRA_SUBJECT,
                            "[Feedback] PaperTrader (${BuildConfig.VERSION_NAME}) (${Auth.uid})"
                        )
                        putExtra(Intent.EXTRA_TEXT, "Hi,\n")
                        selector = selectorIntent
                    }
                    if (emailIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(emailIntent)
                    }
                }) {
                    Icon(Icons.Default.Feedback, "Feedback")
                }
            }
        }, rightButton = {
            IconButton(onClick = { navController.navigate(Screen.Login) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccountCircle, "Account")
                }
            }
        })
    }, floatingActionButton = {
        ExtendedFloatingActionButton(
            modifier = Modifier.semantics { testTag = "new session tag" },
            content = {
            Icon(Icons.Default.Add, "Add")
            Spacer(Modifier.width(8.dp))
            Text("New Session")
        }, onClick = { navController.navigate(Screen.NewSession()) })
    }) {
        Column(Modifier.padding(it)) {
            SwipeRefresh(
                state = loading,
                onRefresh = {
                    CoroutineScope(Dispatchers.IO).launch {
                        updateSessionList()
                    }
                },
            ) {
                Column(
                    Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .fillMaxHeight()
                ) {

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                        if (Auth.isUserAnon) TextButton(
                            onClick = { navController.navigate(Screen.Login) },
                            content = {
                                Text(
                                    "Welcome! Log in to save your progress.",
                                    textAlign = TextAlign.Center
                                )
                            })
                    }

                    Spacer(Modifier.height(16.dp))
                    Text("Sessions", style = AppTypography.headlineLarge)
                    Spacer(Modifier.height(16.dp))
                    if (userSessions.isEmpty()) {
                        Text(
                            "You don't have any sessions yet.\nClick New Session to start.",
                            style = AppTypography.labelLarge
                        )
                    } else {
                        LazyColumn(Modifier.fillMaxHeight()) {
                            items(userSessions) {
                                SessionCard(it, navController)
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }

                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionCard(session: UserSessionData, navController: NavController<Screen>) {
    Card(
        Modifier
            .fillMaxWidth()
            .clickable { navController.navigate(Screen.SessionInfo(session.id)) }
    ) {
        Column(Modifier.padding(8.dp)) {
            if (session.startTS != 0.toLong() && session.endTS != 0.toLong())
                Text(
                    dateTimeText(session.startTS, session.endTS),
                    style = AppTypography.headlineMedium
                )
            else Text("Invalid date", style = AppTypography.headlineMedium)
            Text(
                "Progress: ${
                    completenessPercentText(
                        session.startTS,
                        session.endTS,
                        session.currentTS
                    )
                }",
                style = AppTypography.labelLarge
            )
        }
    }
}


