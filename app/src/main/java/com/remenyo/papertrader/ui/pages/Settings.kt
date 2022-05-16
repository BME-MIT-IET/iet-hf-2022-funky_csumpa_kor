package com.remenyo.papertrader.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults.cardColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.remenyo.papertrader.AnalyticsHelper.trackScreenView
import com.remenyo.papertrader.App
import com.remenyo.papertrader.Auth
import com.remenyo.papertrader.BuildConfig
import com.remenyo.papertrader.Screen
import com.remenyo.papertrader.db.PreferenceKeys
import com.remenyo.papertrader.db.PreferenceKeys.autoSaveIntervalSec_long_default
import com.remenyo.papertrader.db.PreferenceKeys.maxMultiplier_int_default
import com.remenyo.papertrader.db.PreferenceKeys.refreshIntervalMs_long_default
import com.remenyo.papertrader.ui.theme.AppTypography
import com.remenyo.papertrader.ui.theme.colorScheme
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.pop
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(navController: NavController<Screen>) {
    trackScreenView("settings")
    Scaffold(topBar = {
        TopAppBar(text = "Settings", leftButton = { BackButton { navController.pop() } })
    }) {
        LazyColumn(Modifier.padding(it)) {
            item { RefreshSpeedSettings() }
            item { AutoSaveIntervalSettings() }
            item { MultiplierSettings() }
            item { AppInfo() }
        }
    }
}

@Composable
fun AppInfo() {
    SettingsCard(title = "About", description = "PaperTrader") {
        Text("Version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
        Text("UserID: ${Auth.uid}")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsCard(title: String, description: String, content: @Composable () -> Unit = { }) {
    OutlinedCard(
        Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(text = title, style = AppTypography.titleLarge)
            Spacer(Modifier.height(4.dp))
            Text(text = description, style = AppTypography.labelLarge)
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun AutoSaveIntervalSettings() {
    var sliderVal by remember {
        mutableStateOf(
            App.KVStore.decodeLong(
                PreferenceKeys.autoSaveIntervalSec_long, autoSaveIntervalSec_long_default
            ).toFloat()
        )
    }
    val sliderValInt = derivedStateOf { sliderVal.roundToInt() }
    fun updateVal() = App.KVStore.encode(PreferenceKeys.autoSaveIntervalSec_long, sliderValInt.value.toLong())
    SettingsCard(
        title = "Autosave Interval",
        description = "How often should the game do automatically save during trading?"
    ) {
        Column(Modifier.fillMaxWidth()) {
            Text("Interval: ${sliderValInt.value} seconds")
            Slider(
                value = sliderVal, onValueChange = {
                sliderVal = it
                updateVal()
            }, valueRange = 5f..20f, modifier = Modifier.testTag("autoSaveSliderTag").padding(8.dp, 24.dp), steps = 15)
        }
    }
}

@Composable
fun MultiplierSettings() {
    var maxMultiplier by remember {
        mutableStateOf(
            App.KVStore.decodeInt(
                PreferenceKeys.maxMultiplier_int, maxMultiplier_int_default
            ).toFloat()
        )
    }
    val maxMultiplierValInt = derivedStateOf { maxMultiplier.roundToInt() }
    fun updateVal() = App.KVStore.encode(PreferenceKeys.maxMultiplier_int, maxMultiplierValInt.value)
    SettingsCard(title = "Multiplier", description = "Maximum multiplier in game") {
        Column(Modifier.fillMaxWidth()) {
            Text("Max multi: +/- ${maxMultiplierValInt.value}x")
            Slider(value = maxMultiplier, onValueChange = {
                maxMultiplier = it
                updateVal()
            }, valueRange = 1f..20f, modifier = Modifier.padding(8.dp, 24.dp), steps = 20)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefreshSpeedSettings() {
    var refreshIntervalMs by remember {
        mutableStateOf(
            App.KVStore.decodeLong(
                PreferenceKeys.refreshIntervalMs_long, refreshIntervalMs_long_default
            )
        )
    }
    SettingsCard("Refresh rate", "Trading screen refreshes this often") {
        Row {
            Card(
                colors = cardColors(containerColor = if (refreshIntervalMs == 100L) colorScheme.primaryContainer else colorScheme.secondaryContainer),
                modifier = Modifier
                    .clickable {
                        refreshIntervalMs = 100
                        App.KVStore.encode(
                            PreferenceKeys.refreshIntervalMs_long, refreshIntervalMs
                        )
                    }
                    .width(IntrinsicSize.Max)
                    .weight(1f),
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Fast\n(0.1s)",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Card(
                colors = cardColors(containerColor = if (refreshIntervalMs == 500L) colorScheme.primaryContainer else colorScheme.secondaryContainer),
                modifier = Modifier
                    .clickable {
                        refreshIntervalMs = 500
                        App.KVStore.encode(
                            PreferenceKeys.refreshIntervalMs_long, refreshIntervalMs
                        )
                    }
                    .width(IntrinsicSize.Max)
                    .weight(1f),
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Normal\n(0.5s)",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            Card(
                colors = cardColors(containerColor = if (refreshIntervalMs == 1000L) colorScheme.primaryContainer else colorScheme.secondaryContainer),
                modifier = Modifier
                    .clickable {
                        refreshIntervalMs = 1000
                        App.KVStore.encode(
                            PreferenceKeys.refreshIntervalMs_long, refreshIntervalMs
                        )
                    }
                    .width(IntrinsicSize.Max)
                    .weight(1f),
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Slow\n(1s)",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}