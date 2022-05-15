package com.remenyo.papertrader.ui.components.trading

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.remenyo.papertrader.*
import com.remenyo.papertrader.AnalyticsHelper.trackScreenView
import com.remenyo.papertrader.db.PreferenceKeys
import com.remenyo.papertrader.ui.components.BackButton
import com.remenyo.papertrader.ui.components.Chart
import com.remenyo.papertrader.ui.components.OrderCard
import com.remenyo.papertrader.ui.components.TopAppBar
import com.remenyo.papertrader.ui.theme.AppTypography
import com.remenyo.papertrader.ui.theme.RoundShapes
import com.remenyo.papertrader.ui.theme.colorScheme
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.pop
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun Trading(
    navController: NavController<Screen>,
    sessionID: String
) {
    trackScreenView("trading")

    var tradingActive by remember { mutableStateOf(false) }
    val currentSessionID by rememberUpdatedState(sessionID)
    var loading by remember { mutableStateOf(true) }
    var speedUpMultiplier by remember { mutableStateOf<Long>(100) }
    var speedUpBoxDialog by remember { mutableStateOf(false) }
    var speedFloat by remember { mutableStateOf(speedUpMultiplier.toFloat()) }
    val maxMultiplier = App.KVStore.decodeInt(
        PreferenceKeys.maxMultiplier_int, PreferenceKeys.maxMultiplier_int_default
    )

    fun updateSpeedUpMultiplier(value: Float) {
        App.KVStore.encode(
            PreferenceKeys.speedUpMultiplier_long,
            value.toLong()
        )
        speedUpMultiplier = value.toLong()
    }

    DisposableEffect(SessionModel.sessionDone) {
        CoroutineScope(Dispatchers.Default).launch {
            loading = true
            SessionModel.init(currentSessionID)
            loading = false
            while (!SessionModel.sessionDone) {
                delay(
                    App.KVStore.decodeLong(
                        PreferenceKeys.autoSaveIntervalSec_long,
                        PreferenceKeys.autoSaveIntervalSec_long_default
                    ) * 1000L
                )
                SessionModel.saveSession()
            }
        }

        onDispose {
            CoroutineScope(Dispatchers.IO).launch {
                SessionModel.saveSession()
            }
        }
    }

    // this is maybe the funkiest part of the project
    // this side effect implements the scheduled, repetitive recomposition of the ui on every tick.

    LaunchedEffect(
        loading,
        tradingActive,
        SessionModel.sessionDone
    ) { // src: https://stackoverflow.com/a/67570486
        if (!loading && !SessionModel.sessionDone) {

            // init speedupMultiplier
            updateSpeedUpMultiplier(
                App.KVStore.decodeLong(
                    PreferenceKeys.speedUpMultiplier_long,
                    PreferenceKeys.speedUpMultiplier_long_default
                ).toFloat()
            )

            while (tradingActive) { // only schedule new task if trading is enabled (-> not paused)

                var refreshIntervalMs = App.KVStore.decodeLong(
                    PreferenceKeys.refreshIntervalMs_long,
                    PreferenceKeys.refreshIntervalMs_long_default
                )
                speedUpMultiplier = App.KVStore.decodeLong(
                    PreferenceKeys.speedUpMultiplier_long,
                    PreferenceKeys.speedUpMultiplier_long_default
                )
                var advanceSeconds =
                    (refreshIntervalMs.toDouble() / 1000L * speedUpMultiplier.toDouble()).toLong()

                // fix slow update speed
                if (advanceSeconds == 0L) {
                    refreshIntervalMs *= 10
                    advanceSeconds =
                        (refreshIntervalMs.toDouble() / 1000L * speedUpMultiplier.toDouble()).toLong()
                }

                Log.d(
                    "PaperTrader_Trading",
                    "refreshIntervalMs=$refreshIntervalMs speedUpMultiplier=$speedUpMultiplier"
                )
                Log.d("PaperTrader_Trading", "Advancing session by $advanceSeconds seconds")
                SessionModel.Command.advanceSession(advanceSeconds) // the side effect itself

                delay(refreshIntervalMs) // todo substract process time
            }
            SessionModel.saveSession()
        } else if (SessionModel.sessionDone) {
            navController.pop()
        }
    }

    @Composable
    fun SpeedBox() {
        Card(
            Modifier
                .padding(4.dp)
                .height(IntrinsicSize.Min), shape = RoundShapes.large
        ) {
            Row(horizontalArrangement = Arrangement.SpaceAround) {
                Row(
                    Modifier
                        .background(colorScheme.primary)
                        .padding(4.dp, 2.dp)
                        .fillMaxHeight()
                        .clickable { tradingActive = !tradingActive },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (tradingActive) Icon(
                        Icons.Default.Pause,
                        "Pause", tint = colorScheme.onPrimary
                    ) else
                        Icon(Icons.Default.PlayArrow, "Start", tint = colorScheme.onPrimary)
                }
                Divider(
                    color = colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
                Row(
                    Modifier
                        .background(colorScheme.primary)
                        .padding(4.dp, 2.dp)
                        .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        longTimeFromUnixTimestamp(ts = SessionModel.currentTimestamp),
                        color = colorScheme.onPrimary,
                        style = TextStyle(fontWeight = FontWeight.W600)
                    )
                }
                Row(
                    Modifier
                        .background(if (speedUpMultiplier < 60) colorScheme.primaryContainer else colorScheme.errorContainer)
                        .padding(4.dp, 2.dp)
                        .fillMaxHeight()
                        .clickable {
                            speedUpBoxDialog = !speedUpBoxDialog
                            speedFloat = speedUpMultiplier.toFloat()
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${speedUpMultiplier}${if (speedUpMultiplier < 100) "x" else ""}",
                        style = TextStyle(fontWeight = FontWeight.W800)
                    )
                }
            }
        }
    }

    @Composable
    fun SpeedModifier() {
        Slider(
            value = speedFloat,
            onValueChange = {
                speedFloat = it
                updateSpeedUpMultiplier(it)
            },
            valueRange = 1f..200f,
        )
    }

    @Composable
    fun ExecutorBox() {
        var bep by remember { mutableStateOf(SessionModel.currentMarketSellPrice.toString()) }
        var sep by remember { mutableStateOf(SessionModel.currentMarketBuyPrice.toString()) }
        var multi by remember { mutableStateOf(1f) }
        val multiInt = derivedStateOf { multi.roundToInt() }

        var trailingEnabled by remember { mutableStateOf(false) }
        var trailAmount by remember { mutableStateOf("100") }

        val keyboardController = LocalSoftwareKeyboardController.current


        fun bepCorrect() = bep.toDoubleOrNull() != null && bep.toDouble() > 0
        fun sepCorrect() = sep.toDoubleOrNull() != null && sep.toDouble() >= 0
        fun multiCorrect() = multiInt.value != 0
        fun trailCorrect() = trailAmount.toDoubleOrNull() != null && trailAmount.toDouble() > 0

        fun sendLimitOrder() {
            if (bepCorrect() && sepCorrect() && multiCorrect()) {
                SessionModel.Command.addLimitOrderToSession(
                    Order(
                        bep = bep.toDouble(),
                        sep = sep.toDouble(),
                        multi = multiInt.value.toDouble(),
                        trail = if (trailingEnabled && trailCorrect()) trailAmount.toDouble() else 0.0
                    )
                )
                CoroutineScope(Dispatchers.IO).launch { SessionModel.saveSession() }
            }
        }

        fun sendMarketOrder() {
            if (sepCorrect() && multiCorrect()) {
                SessionModel.Command.addMarketOrderToSession(
                    Order(
                        sep = sep.toDouble(),
                        multi = multiInt.value.toDouble(),
                        trail = if (trailingEnabled && trailCorrect()) trailAmount.toDouble() else 0.0
                    )
                )
                CoroutineScope(Dispatchers.IO).launch { SessionModel.saveSession() }
            }
        }

        Column {
            Row {
                TextField(
                    value = bep,
                    onValueChange = { bep = it },
                    label = { Text("BEP") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }),
                    singleLine = true,
                    placeholder = { Text("Where buy happens") },
                    modifier = Modifier.weight(1f).semantics { testTag = "bep szovegmezo" },
                    isError = !bepCorrect() && bep.isNotEmpty(),
                )
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = sep,
                    onValueChange = { sep = it },
                    label = { Text("SEP") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }),
                    singleLine = true,
                    placeholder = { Text("Where sell happens") },
                    modifier = Modifier.weight(1f),
                    isError = !sepCorrect() && sep.isNotEmpty()
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.height(IntrinsicSize.Max),
                verticalAlignment = Alignment.CenterVertically
            ) {
                /*Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceAround,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                }*/
                Text("Trailing stop")
                Spacer(Modifier.width(8.dp))
                Switch(checked = trailingEnabled, onCheckedChange = { trailingEnabled = it })
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = trailAmount,
                    onValueChange = { trailAmount = it },
                    label = { Text("Trail value") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { keyboardController?.hide() }),
                    singleLine = true,
                    placeholder = { Text("Follows price by this amount") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    isError = !trailCorrect() && trailingEnabled,
                    enabled = trailingEnabled
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.height(IntrinsicSize.Max)) {
                OutlinedButton(
                    shape = RoundShapes.medium,
                    onClick = { sendLimitOrder() },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .testTag("limit"),
                    enabled = bepCorrect() && sepCorrect() && multiCorrect() && (!trailingEnabled || trailCorrect())
                ) {
                    Text("Limit @ $bep", textAlign = TextAlign.Center)
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    shape = RoundShapes.medium,
                    onClick = { sendMarketOrder() },
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    enabled = sepCorrect() && multiCorrect() && (!trailingEnabled || trailCorrect())
                ) {
                    Text(
                        "Market @ ${SessionModel.currentMarketBuyPrice}",
                        textAlign = TextAlign.Center
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (multiInt.value == 0) colorScheme.errorContainer else colorScheme.primaryContainer),
                    modifier = Modifier.clickable { multi = 1f }
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Multi")
                        Text(
                            "${multiInt.value}x${if (multiInt.value == 0) "!!!" else ""}",
                            style = TextStyle(fontWeight = FontWeight.W800)
                        )
                    }
                }
                Slider(
                    value = multi,
                    onValueChange = { multi = it },
                    valueRange = -maxMultiplier.toFloat()..+maxMultiplier.toFloat(),
                    modifier = Modifier.padding(10.dp, 0.dp),
                    steps = ((maxMultiplier - 1) * 2) + 1 // i don't understand why do I have to subtract one from maxMultiplier
                )
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            "Session",
            leftButton = {
                BackButton {
                    CoroutineScope(Dispatchers.Main).launch {
                        SessionModel.saveSession()
                        navController.pop()
                    }
                }
            },
            rightButton = {
                if (!SessionModel.sessionDone) {
                    SpeedBox()
                }
            })
    }) { paddingValues ->
        if (loading)
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        else
            Column(Modifier.padding(paddingValues)) {
                Column(Modifier.padding(12.dp)) {
                    if (!SessionModel.sessionDone)
                        if (speedUpBoxDialog) {
                            SpeedModifier()
                            Spacer(Modifier.height(10.dp))
                        }
                    Box(
                        modifier = Modifier.height(230.dp)
                    ) {
                        Chart()
                    }
                    if (!SessionModel.sessionDone) {
                        Spacer(Modifier.height(10.dp))
                        ExecutorBox()
                        Spacer(Modifier.height(10.dp))
                    }
                    Text("Orders", style = AppTypography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                }
                LazyColumn {
                    items(
                        SessionModel.orders_view.sortedBy { it.closeTS }.reversed()
                            .sortedBy { it.tradingScore() }) { order ->
                        Column(Modifier.padding(12.dp, 6.dp)) {
                            OrderCard(
                                o = order,
                                { SessionModel.Command.marketClose(order.id) },
                                { SessionModel.Command.cancelOrder(order.id) },
                                SessionModel.currentMarketSellPrice
                            )
                        }
                    }
                }

            }
    }
}
