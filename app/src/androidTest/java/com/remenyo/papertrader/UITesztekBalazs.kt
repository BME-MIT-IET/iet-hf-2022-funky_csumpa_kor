package com.remenyo.papertrader

import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.with
import androidx.compose.ui.test.*
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
    }

    //handler.post()
        //Thread.sleep(10000)


       /* val r = Runnable {
            igaz() //<-- put your code in here.
        }

        val h = Handler()
        h.postDelayed(r, 10000)*/

        //composeTestRule.(10000, condition = { igaz() })

        //Thread.sleep(10000)
        //SystemClock.sleep(1500);



        //igaz()

    /*fun igaz(): Boolean{
        val handler = Handler(Looper.getMainLooper()).postDelayed({
            composeTestRule.onNodeWithTag("trade").assertIsDisplayed()
            composeTestRule.onNodeWithTag("trade").performClick()
            composeTestRule.onNodeWithTag("bep szovegmezo").assertIsDisplayed()
            return true
        }, 0)
        AsyncTask.execute { composeTestRule.onNodeWithTag("trade").assertIsDisplayed()
            composeTestRule.onNodeWithTag("trade").performClick()
            composeTestRule.onNodeWithTag("bep szovegmezo").assertIsDisplayed() }
    }*/

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