package com.remenyo.papertrader

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
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
import org.junit.Rule
import org.junit.Test
import java.lang.AssertionError
import java.lang.Exception

class UITesztekBalazs {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<App>()
    // use createAndroidComposeRule<YourActivity>() if you need access to
    // an activity


    @Test
    fun beallitasGombMukodik(){
        composeTestRule.onNodeWithTag("settingsButtonTag").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun progressSzazSzazalek(){
        composeTestRule.onNodeWithTag("account").performClick()
        composeTestRule.onNodeWithTag("delete account").performClick()
        composeTestRule.onNodeWithTag("sure to delete account").performClick()
        composeTestRule.waitUntil(15000){
            try {
                //composeTestRule.onNodeWithTag("no sessions").assertIsDisplayed()
                composeTestRule.onNodeWithTag("new session tag").assertIsDisplayed()
            }catch (e: AssertionError){
                return@waitUntil false
            }
            return@waitUntil true
        }
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()
        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }
        composeTestRule.onNodeWithTag("create button tag").performClick()
        composeTestRule.waitUntil(15000){
            try {
                composeTestRule.onNodeWithText("Session info").assertIsDisplayed()
            }catch (e: AssertionError){
                return@waitUntil false
            }
            return@waitUntil true
        }
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitUntil(15000){
            try {
                composeTestRule.onNodeWithTag("sessionCardTag").assertIsDisplayed()
            }catch (e: AssertionError){
                return@waitUntil false
            }
            return@waitUntil true
        }
        composeTestRule.onNodeWithText("Progress: 100%").assertDoesNotExist()
    }

    @Test
    fun progressNemSzazSzazalek(){
        sHour = 8
        eHour = 9
        composeTestRule.onNodeWithTag("account").performClick()
        composeTestRule.onNodeWithTag("delete account").performClick()
        composeTestRule.onNodeWithTag("sure to delete account").performClick()
        composeTestRule.waitUntil(15000){
            try {
                //composeTestRule.onNodeWithTag("no sessions").assertIsDisplayed()
                composeTestRule.onNodeWithTag("new session tag").assertIsDisplayed()
            }catch (e: AssertionError){
                return@waitUntil false
            }
            return@waitUntil true
        }
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()
        composeTestRule.waitUntil( 15000) {
            EllenorizEngedelyez()
        }
        composeTestRule.onNodeWithTag("create button tag").performClick()
        composeTestRule.waitUntil(15000){
            try {
                composeTestRule.onNodeWithText("Session info").assertIsDisplayed()
            }catch (e: AssertionError){
                return@waitUntil false
            }
            return@waitUntil true
        }
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitUntil(15000){
            try {
                composeTestRule.onNodeWithTag("sessionCardTag").assertIsDisplayed()
            }catch (e: AssertionError){
                return@waitUntil false
            }
            return@waitUntil true
        }
        composeTestRule.onNodeWithText("Progress: 100%").assertExists()
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


}