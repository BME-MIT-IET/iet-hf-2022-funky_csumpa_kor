package com.remenyo.papertrader

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.remenyo.papertrader.ui.components.Home
import com.remenyo.papertrader.ui.components.trading.Trading
import com.remenyo.papertrader.ui.theme.AppTheme
import dev.olshevski.navigation.reimagined.navController
import dev.olshevski.navigation.reimagined.rememberNavController
import org.junit.Rule
import org.junit.Test

class UITesztekPatrik {
    @get:Rule
    val composeTestRule = createComposeRule()
    // use createAndroidComposeRule<YourActivity>() if you need access to
    // an activity

    @Test
    fun myTest() {
        // Start the app
        composeTestRule.setContent ( composable = {
            AppTheme {
                Screen.Home
            }
        })

        /*composeTestRule.onNodeWithTag("bep szovegmezo").performTextClearance()
        composeTestRule.onNodeWithTag("bep szovegmezo").performTextInput("1234")

        composeTestRule.onNodeWithTag("limit").assertTextEquals("Limit @ 1234")*/


    }
}