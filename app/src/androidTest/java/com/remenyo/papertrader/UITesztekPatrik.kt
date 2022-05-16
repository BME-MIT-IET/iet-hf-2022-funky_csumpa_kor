package com.remenyo.papertrader

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
    fun randomGombEllenorzes(){
        randomDate=true

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
        randomDate=true

        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }
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


    @OptIn(ExperimentalAnimationApi::class)
    @Test
    fun limitValtoztat() {
        randomDate=true

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
        composeTestRule.onNodeWithTag("bep szovegmezo").assertIsDisplayed()


        composeTestRule.onNodeWithTag("bep szovegmezo").performTextClearance()
        composeTestRule.onNodeWithTag("bep szovegmezo").performTextInput("1234")

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