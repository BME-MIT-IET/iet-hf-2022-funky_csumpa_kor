package com.remenyo.papertrader.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.remenyo.papertrader.API
import com.remenyo.papertrader.AnalyticsHelper
import com.remenyo.papertrader.AnalyticsHelper.trackScreenView
import com.remenyo.papertrader.Screen
import com.remenyo.papertrader.db.realtime.RealtimeDBRepo
import com.remenyo.papertrader.ui.theme.AppTypography
import com.remenyo.papertrader.ui.theme.RoundShapes
import dev.olshevski.navigation.reimagined.NavController
import dev.olshevski.navigation.reimagined.navigate
import dev.olshevski.navigation.reimagined.pop
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

var randomDate=true
var sYear = 2020
var sMonth =3
var sDay = 12
var sHour = 8

var eYear = mutableStateOf(2020)
var eMonth =mutableStateOf(3)
var eDay = mutableStateOf(12)
var eHour = 16

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewSession(navController: NavController<Screen>, start: Long? = null, end: Long? = null) {
    trackScreenView("new_session")

    var sYear by remember { mutableStateOf(sYear) }
    var sMonth by remember { mutableStateOf(sMonth) }
    var sDay by remember { mutableStateOf(sDay) }

    var sHour by remember { mutableStateOf(sHour) }
    var sMinute by remember { mutableStateOf(0) }


    var eYear by remember { eYear}
    var eMonth by remember { eMonth }
    var eDay by remember { eDay }

    var eHour by remember { mutableStateOf(eHour) }
    var eMinute by remember { mutableStateOf(0) }

    // true = start, false = end
    var startOrEndDate by remember { mutableStateOf(false) }

    var loading by remember { mutableStateOf(false) }

    var addOneHour by remember { mutableStateOf(true) }

    var timespanAvailable by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    fun getUnixTsFromDateTime(mYear: Int, mMonth: Int, mDay: Int, mHour: Int, mMinute: Int): Long {
        val c = Calendar.getInstance()
        c.set(mYear, mMonth, mDay, mHour, mMinute)
        return c.timeInMillis
    }

    fun getUnixTsFromDate(mYear: Int, mMonth: Int, mDay: Int): Long {
        val c = Calendar.getInstance()
        c.set(mYear, mMonth, mDay, 0, 0)
        return c.timeInMillis
    }

    fun startTSUnix() = getUnixTsFromDateTime(
        sYear,
        sMonth,
        sDay,
        sHour,
        sMinute
    )

    fun endTSUnix() = getUnixTsFromDateTime(
        eYear,
        eMonth,
        eDay,
        eHour,
        eMinute
    )

    fun ensureTSNotNegative() {
        if (getUnixTsFromDate(eYear, eMonth, eDay) - getUnixTsFromDate(
                sYear, sMonth, sDay
            ) < 0
        ) {
            eYear = sYear.also { sYear = eYear }
            eMonth = sMonth.also { sMonth = eMonth }
            eDay = sDay.also { sDay = eDay }
        }

        if ((eHour * 60 + eMinute) - (sHour * 60 + sMinute) < 0) {
            eHour = sHour.also { sHour = eHour }
            eMinute = sMinute.also { sMinute = eMinute }
        }
    }

    // run availability check on change
    LaunchedEffect(
        sYear,
        sMonth,
        sDay,
        sHour,
        sMinute,
        eYear,
        eMonth,
        eDay,
        eHour,
        eMinute
    ) {
        ensureTSNotNegative()
        loading = true
        timespanAvailable = false
        try {
            timespanAvailable =
                API.getAvailability(startTSUnix().floorDiv(1000), endTSUnix().floorDiv(1000))
        } catch (e: Exception) {
            Log.e("PaperTrader_NewSession", e.toString(), e)
            AnalyticsHelper.reportException(e)
            throw e
        }
        delay(200L)
        // todo specify delay including actual processing time
        loading = false
    }

    fun setStartFromUnix(ts: Long, justDate: Boolean = false) {
        val c = Calendar.getInstance()
        c.time = Date(ts)
        sYear = c.get(Calendar.YEAR)
        sMonth = c.get(Calendar.MONTH)
        sDay = c.get(Calendar.DAY_OF_MONTH)
        if (!justDate) {
            sHour = c.get(Calendar.HOUR_OF_DAY)
            sMinute = c.get(Calendar.MINUTE)
        }
    }

    fun setEndFromUnix(ts: Long, justDate: Boolean = false) {
        val c = Calendar.getInstance()
        c.time = Date(ts)
        eYear = c.get(Calendar.YEAR)
        eMonth = c.get(Calendar.MONTH)
        eDay = c.get(Calendar.DAY_OF_MONTH)
        if (!justDate) {
            eHour = c.get(Calendar.HOUR_OF_DAY)
            eMinute = c.get(Calendar.MINUTE)
        }
    }

    fun randomizeDate() {
        // 2015.12.31. - 2021.10.25.
        // 1451606340 - 1635151740
        // start from 2020.01.01 - 1577836800 instead to not include lame data
        val ts = Random.nextLong(1577836800000, 1635151740000)
        setStartFromUnix(ts, true)
        setEndFromUnix(ts, true)

    }

    LaunchedEffect(false) {
        if (start != null && end != null) {
            setStartFromUnix(start * 1000L)
            setEndFromUnix(end * 1000L)
        } else{
            if(randomDate)
                randomizeDate()
        }
    }

    val dateFormat = SimpleDateFormat.getDateInstance()
    val timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)

    fun makeDatePickerDialog(context: Context): DatePickerDialog {
        return DatePickerDialog(
            context,
            { _: DatePicker, mYear: Int, mMonth: Int, mDay: Int ->
                if (startOrEndDate) {
                    sYear = mYear
                    sMonth = mMonth
                    sDay = mDay
                } else {
                    eYear = mYear
                    eMonth = mMonth
                    eDay = mDay
                }
            },
            if (startOrEndDate) sYear else eYear,
            if (startOrEndDate) sMonth else eMonth,
            if (startOrEndDate) sDay else eDay
        )
    }

    fun makeTimePickerDialog(context: Context): TimePickerDialog {
        return TimePickerDialog(
            context, { _, mHour, mMinute ->
                if (startOrEndDate) {
                    sHour = mHour
                    sMinute = mMinute
                } else {
                    eHour = mHour
                    eMinute = mMinute
                }
            },
            if (startOrEndDate) sHour else eHour,
            if (startOrEndDate) sMinute else eMinute, true
        )
    }

    Scaffold(topBar = {
        TopAppBar("New Session", {
            BackButton {
                navController.pop()
            }
        })
    }) {
        Column(
            Modifier
                .padding(it)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Date", style = AppTypography.headlineLarge)
            Spacer(Modifier.height(16.dp))
            Row {
                OutlinedButton(
                    shape = RoundShapes.medium,
                    modifier = Modifier.weight(1f).testTag("first date"),
                    onClick = {
                        startOrEndDate = true
                        makeDatePickerDialog(context).show()
                    }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Start date")
                        Text(
                            style = AppTypography.titleLarge, text =
                            dateFormat.format(getUnixTsFromDate(sYear, sMonth, sDay))
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    shape = RoundShapes.medium,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        startOrEndDate = false
                        makeDatePickerDialog(context).show()
                    }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "End date")
                        Text(
                            style = AppTypography.titleLarge, text =
                            dateFormat.format(getUnixTsFromDate(eYear, eMonth, eDay))
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                modifier = Modifier.fillMaxWidth().testTag("randomize"),
                shape = RoundShapes.medium,
                onClick = { randomizeDate() }) {
                Text("Randomize Date", textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(32.dp))

            Text("Time", style = AppTypography.headlineLarge)

            Spacer(Modifier.height(16.dp))
            Row {
                OutlinedButton(
                    shape = RoundShapes.medium,
                    modifier = Modifier.weight(1f).testTag("start time"),
                    onClick = {
                        startOrEndDate = true
                        makeTimePickerDialog(context).show()
                    }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Start time")
                        Text(
                            style = AppTypography.titleLarge, text =
                            timeFormat.format(startTSUnix())
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(
                    shape = RoundShapes.medium,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        startOrEndDate = false
                        makeTimePickerDialog(context).show()
                    }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "End time")
                        Text(
                            style = AppTypography.titleLarge, text = timeFormat.format(endTSUnix())
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(modifier=Modifier.testTag("plus one hour") ,checked = addOneHour, onCheckedChange = { addOneHour = it })
                Text(
                    "Start from T+1h (Have price history at start)",
                    modifier = Modifier.clickable { addOneHour = !addOneHour })
            }
            Spacer(Modifier.height(24.dp))
            when {
                loading -> {
                    Text("Checking availability...")
                }
                timespanAvailable -> {
                    Text("The specified timespan is fully available")
                }
                else -> Text("The specified timespan is not available.", Modifier.testTag("not available"))
            }
            Spacer(Modifier.height(16.dp))
            Button(
                enabled = !loading && timespanAvailable,
                modifier = Modifier.fillMaxWidth().semantics { testTag = "create button tag" },
                onClick = {
                    coroutineScope.launch {
                        loading = true
                        val (sessionID, successful) = RealtimeDBRepo.createSession(
                            startTSUnix().floorDiv(1000),
                            endTSUnix().floorDiv(1000),
                            startTSUnix().floorDiv(1000) + if (addOneHour) 3600 else 60
                        )
                        if (successful) {
                            // todo navigate to sessioninfo
                            navController.pop()
                            navController.navigate(Screen.SessionInfo(sessionID))
                        } /*else {
// todo toast fail
                        }*/
                        loading = false
                    }
                }) {
                if (!loading) {
                    Text(text = "Create session")
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    }
}