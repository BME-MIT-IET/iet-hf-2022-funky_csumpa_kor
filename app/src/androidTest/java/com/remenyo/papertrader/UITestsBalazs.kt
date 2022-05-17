package com.remenyo.papertrader

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.ui.geometry.Offset
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

class UITestsBalazs {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<App>()

    @Test
    fun beallitasButtonWorking(){
        composeTestRule.onNodeWithTag("settingsButtonTag").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun progressNotFinished(){
        sHour = 8
        eHour = 16
        composeTestRule.onNodeWithTag("account").performClick()
        composeTestRule.onNodeWithTag("delete account").performClick()
        composeTestRule.onNodeWithTag("sure to delete account").performClick()
        composeTestRule.waitUntil(15000){
            componentWithTextLoaded("New Session")
        }
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()
        composeTestRule.waitUntil( 15000) {
            componentWithTextLoaded("Create session")
        }
        composeTestRule.onNodeWithTag("create button tag").performClick()
        composeTestRule.waitUntil(15000){
            componentWithTextLoaded("Session info")
        }
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitUntil(15000){
            componentWithTextLoaded("Sessions")
        }
        composeTestRule.onNodeWithText("Progress: 100%").assertDoesNotExist()
    }

    @Test
    fun progressFinished(){
        sHour = 8
        eHour = 9
        composeTestRule.onNodeWithTag("account").performClick()
        composeTestRule.onNodeWithTag("delete account").performClick()
        composeTestRule.onNodeWithTag("sure to delete account").performClick()
        composeTestRule.waitUntil(15000){
            componentWithTextLoaded("New Session")
        }
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()
        composeTestRule.waitUntil( 15000) {
            componentWithTextLoaded("Create session")
        }
        composeTestRule.onNodeWithTag("create button tag").performClick()
        composeTestRule.waitUntil(15000){
            componentWithTextLoaded("Session info")
        }
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitUntil(15000){
            componentWithTextLoaded("Sessions")
        }
        composeTestRule.onNodeWithText("Progress: 100%").assertExists()
    }

    @Test
    fun autoSaveSliderTest(){
        composeTestRule.onNodeWithTag("settingsButtonTag").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithTag("autoSaveSliderTag").performTouchInput {
            click(Offset(centerRight.x, centerY))
        }
        composeTestRule.onNodeWithText("Interval: 20 seconds").assertIsDisplayed()
        composeTestRule.onNodeWithTag("autoSaveSliderTag").performTouchInput {
            click(Offset(centerLeft.x, centerY))
        }
        composeTestRule.onNodeWithText("Interval: 5 seconds").assertIsDisplayed()
    }

    @Test
    fun settingsMultiplierSliderLeftTest(){
        sHour = 8
        eHour = 16
        composeTestRule.onNodeWithTag("settingsButtonTag").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithTag("multiplierSliderTag").performTouchInput {
            click(Offset(centerLeft.x, centerY))
        }
        composeTestRule.onNodeWithText("Max multi: +/- 1x").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitUntil(15000){
            componentWithTextLoaded("New Session")
        }
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()
        composeTestRule.waitUntil( 15000) {
            componentWithTextLoaded("Create session")
        }
        composeTestRule.onNodeWithTag("create button tag").performClick()
        composeTestRule.waitUntil(15000){
            componentWithTextLoaded("Session info")
        }
        composeTestRule.waitUntil( 15000) {
            componentWithTextLoaded("Trade")
        }
        composeTestRule.onNodeWithTag("trade").performClick()
        composeTestRule.waitUntil( 15000) {
            componentWithTextLoaded("Session")
        }
        composeTestRule.onNodeWithTag("sessionMultiSliderTag").performTouchInput {
            click(Offset(centerRight.x, centerY))
        }
        composeTestRule.onNodeWithText("1x").assertIsDisplayed()
    }

    @Test
    fun settingsMultiplierSliderRightTest(){
        composeTestRule.onNodeWithTag("settingsButtonTag").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithTag("multiplierSliderTag").performTouchInput {
            click(Offset(centerRight.x, centerY))
        }
        composeTestRule.onNodeWithText("Max multi: +/- 20x").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        composeTestRule.waitUntil(15000){
            componentWithTextLoaded("New Session")
        }
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()
        composeTestRule.waitUntil( 15000) {
            componentWithTextLoaded("Create session")
        }
        composeTestRule.onNodeWithTag("create button tag").performClick()
        composeTestRule.waitUntil(15000){
            componentWithTextLoaded("New Session")
        }
        composeTestRule.waitUntil( 15000) {
            componentWithTextLoaded("Trade")
        }
        composeTestRule.onNodeWithTag("trade").performClick()
        composeTestRule.waitUntil( 15000) {
            componentWithTextLoaded("Session")
        }
        composeTestRule.onNodeWithTag("sessionMultiSliderTag").performTouchInput {
            click(Offset(centerRight.x, centerY))
        }
        composeTestRule.onNodeWithText("20x").assertIsDisplayed()
    }

    private fun componentWithTextLoaded(string: String): Boolean{
        try {
            composeTestRule.onNodeWithText(string).assertIsDisplayed()
        }catch (e: AssertionError){
            return false
        }
        return true
    }
}