package com.remenyo.papertrader.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.remenyo.papertrader.AnalyticsHelper.trackScreenView
import com.remenyo.papertrader.Auth
import com.remenyo.papertrader.Screen
import com.remenyo.papertrader.db.realtime.RealtimeDBRepo.deleteUserWithSessions
import com.remenyo.papertrader.ui.theme.AppTypography
import com.remenyo.papertrader.ui.theme.RoundShapes
import com.remenyo.papertrader.ui.theme.colorScheme
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.pop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Login(navController: NavController<Screen>) {
    trackScreenView("login")

    var email by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var openEmailDialog by remember { mutableStateOf(false) }
    var deleteUserConfirmDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    if (openEmailDialog) {
        AlertDialog(
            onDismissRequest = { openEmailDialog = false },
            icon = { Icon(Icons.Default.Email, "Email") },
            title = {
                Text("Link sent", style = AppTypography.titleLarge)
            },
            text = { Text(text = "Open your email app and click the sent link to login.") },
            confirmButton = {
                TextButton(onClick = {
                    openEmailDialog = false
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_APP_EMAIL)
                    context.startActivity(intent)
                }) {
                    Text("Open")
                }
            },
            dismissButton = {
                TextButton(onClick = { openEmailDialog = false }) {
                    Text("Back")
                }
            })
    }

    if (deleteUserConfirmDialog) {
        AlertDialog(
            onDismissRequest = { deleteUserConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, "Warning") },
            title = {
                Text("Are you sure?", style = AppTypography.titleLarge)
            },
            text = { Text(text = "By deleting your account, all user data will be deleted irrevocably.") },
            confirmButton = {
                TextButton(onClick = {
                    deleteUserConfirmDialog = false
                    CoroutineScope(Dispatchers.IO).launch {
                        loading = true
                        deleteUserWithSessions(Auth.uid)
                        Auth.deleteAccount()
                        loading = false
                        navController.pop()
                    }
                }) {
                    Text("Delete", color = colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteUserConfirmDialog = false }) {
                    Text("Back")
                }
            })
    }

    Scaffold(topBar = {
        TopAppBar(text = "Account", leftButton = { BackButton { navController.pop() } })
    }) {
        Column(
            Modifier
                .padding(48.dp, 0.dp, 48.dp, 36.dp)
                .fillMaxWidth()
                .fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (Auth.signedIn) {
                Text(
                    "Logged in ${
                        if (Auth.isUserAnon) {
                            "anonymously"
                        } else {
                            Auth.userEmail
                        }
                    }"
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            if (!Auth.signedIn || (Auth.signedIn && Auth.isUserAnon)) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    label = {
                        Text(
                            text = if (Auth.signedIn && Auth.isUserAnon) {
                                "Connect with email"
                            } else {
                                "Email"
                            }
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    placeholder = { Text("user@mail.com") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(modifier = Modifier.height(48.dp))
            if (Auth.signedIn && !Auth.isUserAnon) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            loading = true
                            Auth.signOut()
                            navController.pop()
                            loading = false
                        }
                    }, modifier = Modifier.fillMaxWidth(), shape = RoundShapes.small
                ) {
                    Text(text = "Sign out")
                }
            } else {
                Button(
                    onClick = {
                        if (email.isNotEmpty()) {
                            loading = true
                            coroutineScope.launch {
                                if (Auth.startEmailLogin(email)) {
                                    openEmailDialog = true
                                }
                                loading = false
                                if (Auth.signedIn) {
                                    Toast.makeText(
                                        context,
                                        "Failed to sign in / sign up",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Failed to login",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundShapes.small,
                    enabled = !loading
                ) {
                    if (!loading) {
                        Text(
                            text = if (Auth.signedIn) {
                                "Sign In / Sign Up"
                            } else {
                                "Login"
                            }
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                }
                Spacer(Modifier.height(8.dp))
                // src: https://stackoverflow.com/a/67244131
                val annotatedText = buildAnnotatedString {
                    append("By signing up, you agree to the")
                    pushStringAnnotation(
                        tag = "ToS",// provide tag which will then be provided when you click the text
                        annotation = "ToS"
                    )
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.primary,
                        )
                    ) {
                        append(" Terms of Service")
                    }
                    append(" and")
                    pushStringAnnotation(
                        tag = "PrivacyPolicy",// provide tag which will then be provided when you click the text
                        annotation = "PrivacyPolicy"
                    )
                    withStyle(
                        style = SpanStyle(
                            color = colorScheme.primary,
                        )
                    ) {
                        append(" Privacy Policy")
                    }
                    append(".")
                    pop()
                }
                ClickableText(
                    text = annotatedText,
                    onClick = { offset ->
                        annotatedText.getStringAnnotations(
                            tag = "ToS",// tag which you used in the buildAnnotatedString
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let {
                            //do your stuff when it gets clicked
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://traderapp-androidhf.web.app/terms_of_service.html")
                            )
                            if (intent.resolveActivity(context.packageManager) != null) context.startActivity(
                                intent
                            )
                        }
                        annotatedText.getStringAnnotations(
                            tag = "PrivacyPolicy",// tag which you used in the buildAnnotatedString
                            start = offset,
                            end = offset
                        ).firstOrNull()?.let {
                            //do your stuff when it gets clicked
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://traderapp-androidhf.web.app/privacy_policy.html")
                            )
                            if (intent.resolveActivity(context.packageManager) != null) context.startActivity(
                                intent
                            )
                        }

                    },
                    style = AppTypography.labelSmall
                )
            }
            Spacer(Modifier.height(16.dp))
            if (Auth.signedIn) {
                TextButton(
                    onClick = {
                        deleteUserConfirmDialog = true
                        // todo toast account deleted
                    }, enabled = !loading
                ) {
                    Text(text = "Delete account", style = TextStyle(color = colorScheme.error))
                }
            } else {
                TextButton(onClick = {
                    loading = true
                    coroutineScope.launch {
                        if (Auth.anonymousLogin()) {
                            navController.pop()
                        }
                        loading = false
                    }
                }, enabled = !loading) {
                    Text(text = "Anonymous login")
                }
            }
        }
    }
}