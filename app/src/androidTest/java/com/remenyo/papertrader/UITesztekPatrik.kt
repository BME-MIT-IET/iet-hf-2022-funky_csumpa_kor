package com.remenyo.papertrader

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import com.remenyo.papertrader.ui.components.*
import com.remenyo.papertrader.ui.components.trading.Trading
import com.remenyo.papertrader.ui.theme.AppTheme
import dev.olshevski.navigation.reimagined.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UITesztekPatrik {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<App>()
    //val composeTestRule = createComposeRule()
    // use createAndroidComposeRule<YourActivity>() if you need access to
    // an activity

    @Before
    fun lateinitBetoltes(){
        /*composeTestRule.setContent ( composable = {
            AppTheme {
                Trading(navController = rememberNavController(initialBackstack = mutableListOf()), sessionID = "-N27AMb0MImgNBS_Pkfq")
            }
        })*/
    }

    @Test
    fun myTest() {
        // Start the app

        //(composeTestRule.activity as App)

        //composeTestRule.onNodeWithTag("udvozol").assertTextEquals("Welcome! Log in to save your progress.")

        /*composeTestRule.onNodeWithTag("bep szovegmezo").performTextClearance()
        composeTestRule.onNodeWithTag("bep szovegmezo").performTextInput("1234")

        composeTestRule.onNodeWithTag("limit").assertTextEquals("Limit @ 1234")*/


        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("trade button tag").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trade button tag").performClick()

        //composeTestRule.onNodeWithTag("bep szovegmezo").performTextClearance()
        //composeTestRule.onNodeWithTag("bep szovegmezo").performTextInput("1234")

        // composeTestRule.onNodeWithTag("limit").assertTextEquals("Limit @ 1234")


    }
}