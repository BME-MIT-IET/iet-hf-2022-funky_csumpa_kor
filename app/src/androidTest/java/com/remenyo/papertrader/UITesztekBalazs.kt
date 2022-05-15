package com.remenyo.papertrader

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.remenyo.papertrader.ui.components.*
import com.remenyo.papertrader.ui.components.trading.Trading
import com.remenyo.papertrader.ui.theme.AppTheme
import dev.olshevski.navigation.reimagined.*
import org.junit.Rule
import org.junit.Test

class UITesztekBalazs {
    @get:Rule
    val composeTestRule = createComposeRule()
    // use createAndroidComposeRule<YourActivity>() if you need access to
    // an activity

    @OptIn(ExperimentalAnimationApi::class)
    @Test
    fun myTest() {
        // Start the app

        composeTestRule.setContent ( composable = {
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
        })
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("trade button tag").assertIsDisplayed()
        composeTestRule.onNodeWithTag("trade button tag").performClick()

        //composeTestRule.onNodeWithTag("bep szovegmezo").performTextClearance()
        //composeTestRule.onNodeWithTag("bep szovegmezo").performTextInput("1234")

       // composeTestRule.onNodeWithTag("limit").assertTextEquals("Limit @ 1234")


    }
}