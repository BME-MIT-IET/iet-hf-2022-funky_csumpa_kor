package com.remenyo.papertrader

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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.lang.AssertionError

class UITesztekPatrik {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<App>()
    //val composeTestRule = createComposeRule()
    // use createAndroidComposeRule<YourActivity>() if you need access to
    // an activity
    

    @OptIn(ExperimentalAnimationApi::class)
    @Test
    fun myTest() {
        /*composeTestRule.setContent ( composable = {
            var navController = rememberNavController(mutableListOf<Screen>(Screen.Home))
            AppTheme {
                AnimatedNavHost(navController, transitionSpec = { action, _, _ ->
                    val direction = if (action == NavAction.Pop) {
                        AnimatedContentScope.SlideDirection.Down
                    } else {
                        AnimatedContentScope.SlideDirection.Up
                    }
                    slideIntoContainer(direction) with slideOutOfContainer(direction)
                }) { screen ->
                    when (screen) {
                        is Screen.Login -> Login(navController)
                        is Screen.Home -> Home(navController)
                        is Screen.NewSession -> NewSession(
                            navController, screen.start, screen.end
                        )
                        is Screen.SessionInfo -> SessionInfo(
                            navController, screen.id
                        )
                        is Screen.SessionPlay -> Trading(
                            navController, screen.id
                        )
                        is Screen.Settings -> Settings(navController)
                    }
                }
            }
        })*/

        MMKV.initialize(InstrumentationRegistry.getInstrumentation().context)
        App.KVStore = MMKV.mmkvWithID("App")

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