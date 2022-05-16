package com.remenyo.papertrader

import android.util.Log
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.InstrumentationRegistry.getTargetContext
import androidx.test.platform.app.InstrumentationRegistry
import com.remenyo.papertrader.ui.components.*
import com.remenyo.papertrader.ui.components.trading.Trading
import com.remenyo.papertrader.ui.theme.AppTheme
import com.tencent.mmkv.MMKV
import dev.olshevski.navigation.reimagined.*
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.AssertionError
import java.text.SimpleDateFormat
import java.util.*

class UITesztekPatrik {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<App>()
    //val composeTestRule = createComposeRule()
    // use createAndroidComposeRule<YourActivity>() if you need access to
    // an activity

    fun getUnixTsFromDate(mYear: Int, mMonth: Int, mDay: Int): Long {
        val c = Calendar.getInstance()
        c.set(mYear, mMonth, mDay, 0, 0)
        return c.timeInMillis
    }

    @Test
    fun munkamenetInformacioOldal(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }

        composeTestRule.onNodeWithTag("create button tag").performClick()

        composeTestRule.waitUntil( 15000) {
            Ellenoriz()
        }
        composeTestRule.onNodeWithTag("trade").performClick()
        composeTestRule.onNodeWithTag("market").performClick()
        composeTestRule.onNodeWithTag("limit").performClick()
        composeTestRule.onNodeWithTag("back").performClick()

        composeTestRule.waitUntil( 15000) {
            try {
                composeTestRule.onNodeWithTag("not yet opened").assertIsDisplayed()
                composeTestRule.onNodeWithTag("opened").assertIsDisplayed()
            }catch (e: AssertionError){
                return@waitUntil false
            }
            return@waitUntil true
        }
    }

    @Test
    fun limitCsempe(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }

        composeTestRule.onNodeWithTag("create button tag").performClick()

        composeTestRule.waitUntil( 15000) {
            Ellenoriz()
        }
        composeTestRule.onNodeWithTag("trade").performClick()
        composeTestRule.onNodeWithTag("limit").performClick()
        composeTestRule.waitUntil( 15000) {
            try {
                //composeTestRule.onNodeWithTag("not yet opened").assertIsDisplayed()
                composeTestRule.onNodeWithTag("cancel").assertIsDisplayed()

            }catch (e: AssertionError){
                return@waitUntil false
            }
            return@waitUntil true
        }

        composeTestRule.onNodeWithTag("cancel").performClick()

        composeTestRule.waitUntil( 15000) {
            try {
                composeTestRule.onNodeWithTag("cancelled").assertIsDisplayed()
                //composeTestRule.onNodeWithTag("cancel").assertIsNotDisplayed()
            }catch (e: AssertionError){
                //composeTestRule.onNodeWithTag("cancel").performClick()
                return@waitUntil false
            }
            return@waitUntil true
        }
    }

    @Test
    fun marketCsempe(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }

        composeTestRule.onNodeWithTag("create button tag").performClick()

        composeTestRule.waitUntil( 15000) {
            Ellenoriz()
        }
        composeTestRule.onNodeWithTag("trade").performClick()
        composeTestRule.onNodeWithTag("market").performClick()
        composeTestRule.waitUntil( 15000) {
            try {
                //composeTestRule.onNodeWithTag("opened").assertIsDisplayed()
                composeTestRule.onNodeWithTag("market close").assertIsDisplayed()
            }catch (e: AssertionError){
                return@waitUntil false
            }
            return@waitUntil true
        }

        composeTestRule.onNodeWithTag("market close").performClick()

        composeTestRule.waitUntil( 15000) {
            try {
                composeTestRule.onNodeWithTag("closed").assertIsDisplayed()
            }catch (e: AssertionError){
                return@waitUntil false
            }
            return@waitUntil true
        }
    }

    @Test
    fun helytelenBep(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }

        composeTestRule.onNodeWithTag("create button tag").performClick()

        composeTestRule.waitUntil( 15000) {
            Ellenoriz()
        }
        composeTestRule.onNodeWithTag("trade").performClick()

        composeTestRule.onNodeWithTag("bep").performTextReplacement("éáőúűóüö")
        composeTestRule.onNodeWithTag("limit").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("market").assertIsEnabled()
        composeTestRule.onNodeWithTag("bep").performTextReplacement("123")
        composeTestRule.onNodeWithTag("limit").assertIsEnabled()
    }

    @Test
    fun helytelenSep(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }

        composeTestRule.onNodeWithTag("create button tag").performClick()

        composeTestRule.waitUntil( 15000) {
            Ellenoriz()
        }
        composeTestRule.onNodeWithTag("trade").performClick()

        composeTestRule.onNodeWithTag("sep").performTextReplacement("éáőúűóüö")
        composeTestRule.onNodeWithTag("limit").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("market").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("sep").performTextReplacement("123")
        composeTestRule.onNodeWithTag("limit").assertIsEnabled()
        composeTestRule.onNodeWithTag("market").assertIsEnabled()

    }

    @Test
    fun helytelenKoveto(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }

        composeTestRule.onNodeWithTag("create button tag").performClick()

        composeTestRule.waitUntil( 15000) {
            Ellenoriz()
        }
        composeTestRule.onNodeWithTag("trade").performClick()

        composeTestRule.onNodeWithTag("trailing stop").performClick()
        composeTestRule.onNodeWithTag("trail value").assertIsEnabled()
        composeTestRule.onNodeWithTag("trail value").performTextReplacement("éáőúűó")
        composeTestRule.onNodeWithTag("limit").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("market").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("trail value").performTextReplacement("123")
        composeTestRule.onNodeWithTag("limit").assertIsEnabled()
        composeTestRule.onNodeWithTag("market").assertIsEnabled()

    }

    @Test
    fun kovetoBeallit(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }

        composeTestRule.onNodeWithTag("create button tag").performClick()

        composeTestRule.waitUntil( 15000) {
            Ellenoriz()
        }
        composeTestRule.onNodeWithTag("trade").performClick()

        composeTestRule.onNodeWithTag("trailing stop").performClick()
        composeTestRule.onNodeWithTag("trail value").assertIsEnabled()
        composeTestRule.onNodeWithTag("trailing stop").performClick()
        composeTestRule.onNodeWithTag("trail value").assertIsNotEnabled()
    }

    @Test
    fun randomGombEllenorzes(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }

        composeTestRule.onNodeWithTag("randomize").performClick()

        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }

        var tartalmazas=false
        try {
            composeTestRule.onNodeWithTag("first date")
                .assertTextContains(SimpleDateFormat.getDateInstance()
                    .format(getUnixTsFromDate(sYear, sMonth, sDay)))
        }catch (e: AssertionError){
            tartalmazas=true
        }
        if(!tartalmazas){
            throw AssertionError("A dátum nem változott meg a randomizálás gombra")
        }


    }

    @Test
    fun letrehozSessionSikeres(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }
    }

    @Test
    fun letrehozSessionPlusz1oraBepipalva(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }

        try {
            composeTestRule.onNodeWithTag("plus one hour").assertIsOn()
        }catch (e: AssertionError){
            composeTestRule.onNodeWithTag("plus one hour").performClick()
        }

        val startTime = composeTestRule.onNodeWithTag("start time")
        var noveltIdo:Int=0

        for ((key, value) in startTime.fetchSemanticsNode().config) {
            if (key.name == "Text"){
                var eredmeny = value.toString()
                eredmeny=eredmeny.substring(13, eredmeny.indexOf(':'))
                noveltIdo=eredmeny.toInt()+1
            }
        }

        composeTestRule.onNodeWithTag("create button tag").performClick()

        composeTestRule.waitUntil( 15000) {
            Ellenoriz()
        }
        composeTestRule.onNodeWithTag("trade").performClick()

        composeTestRule.onNodeWithTag("active time").assertTextContains("$noveltIdo:0",true)
    }

    @Test
    fun letrehozSessionPlusz1oraNincsPipa() {
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }

        try {
            composeTestRule.onNodeWithTag("plus one hour").assertIsOff()
        }catch (e: AssertionError){
            composeTestRule.onNodeWithTag("plus one hour").performClick()
        }

        val startTime = composeTestRule.onNodeWithTag("start time")
        var ido:Int=0

        for ((key, value) in startTime.fetchSemanticsNode().config) {
            if (key.name == "Text"){
                var eredmeny = value.toString()
                eredmeny=eredmeny.substring(13, eredmeny.indexOf(':'))
                ido=eredmeny.toInt()
            }
        }

        composeTestRule.onNodeWithTag("create button tag").performClick()

        composeTestRule.waitUntil( 15000) {
            Ellenoriz()
        }
        composeTestRule.onNodeWithTag("trade").performClick()

        composeTestRule.onNodeWithTag("active time").assertTextContains("$ido:0",true)
    }

    @Test
    fun letrehozSessionSikertelen(){
        randomDate=false
        sYear=  2022
        sMonth=  5
        sDay=  16
        eYear= mutableStateOf( 2022)
        eMonth= mutableStateOf( 5)
        eDay= mutableStateOf( 16)

        composeTestRule.onNodeWithTag("new session tag").performClick()

        composeTestRule.waitUntil( 15000) {
            try {
                composeTestRule.onNodeWithTag("not available").assertIsDisplayed()
            }catch (e: AssertionError){
                return@waitUntil false
            }
            return@waitUntil true
        }
        //composeTestRule.onNodeWithTag("create button tag").performClick()

        randomDate=true

    }

    @Test
    fun felhasznaloTorles(){
        composeTestRule.onNodeWithTag("account").performClick()
        composeTestRule.onNodeWithTag("delete account").performClick()
        composeTestRule.onNodeWithTag("sure to delete account").performClick()
        //composeTestRule.onNodeWithTag("sure to delete account").perform

        composeTestRule.waitUntil(15000){
            try {
                composeTestRule.onNodeWithTag("no sessions").assertIsDisplayed()
            }catch (e: AssertionError){
                return@waitUntil false
            }
            return@waitUntil true
        }

        //composeTestRule.onNodeWithTag("trade")
    }

    @Test
    fun limitValtoztat() {
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }

        composeTestRule.onNodeWithTag("create button tag").performClick()

        composeTestRule.waitUntil( 15000) {
            Ellenoriz()
        }
        composeTestRule.onNodeWithTag("trade").performClick()
        composeTestRule.onNodeWithTag("bep").assertIsDisplayed()


        composeTestRule.onNodeWithTag("bep").performTextClearance()
        composeTestRule.onNodeWithTag("bep").performTextInput("1234")

        composeTestRule.waitUntil( 15000) {
            EllenorizLimit()
        }

        composeTestRule.onNodeWithTag("limit").assertTextEquals("Limit @ 1234")
    }


    fun Ellenoriz(): Boolean{
        try{
            composeTestRule.onNodeWithTag("trade").assertIsDisplayed()
        }catch (e: AssertionError){
            return false
        }
        return true
    }

    fun EllenorizEngedelyez():Boolean{
        try {
            composeTestRule.onNodeWithTag("create button tag").assertIsEnabled()
        }catch (e: AssertionError){
            return false
        }
        return true
    }

    fun EllenorizLimit():Boolean{
        try {
            composeTestRule.onNodeWithTag("limit").assertExists()
        }catch (e: AssertionError){
            return false
        }
        return true
    }




}