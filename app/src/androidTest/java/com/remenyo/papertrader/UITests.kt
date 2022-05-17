package com.remenyo.papertrader

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.remenyo.papertrader.ui.components.*
import org.junit.Rule
import org.junit.Test
import java.lang.AssertionError
import java.text.SimpleDateFormat
import java.util.*

class UITests {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<App>()

    fun getUnixTsFromDate(mYear: Int, mMonth: Int, mDay: Int): Long {
        val c = Calendar.getInstance()
        c.set(mYear, mMonth, mDay, 0, 0)
        return c.timeInMillis
    }

    //Idő alapú tesztet nem tudunk tesztelni
    /*
    @Test
    fun normalSpeed(){
        composeTestRule.onNodeWithTag("settingsButtonTag").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
        composeTestRule.onNodeWithTag("slow").performClick()
        composeTestRule.onNodeWithTag("back").performClick()

        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        Waiting({composeTestRule.onNodeWithTag("create button tag").assertIsEnabled()})

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

        Waiting({composeTestRule.onNodeWithTag("trade").assertIsDisplayed()})
        composeTestRule.onNodeWithTag("trade").performClick()

        composeTestRule.onNodeWithTag("trading active").performClick()

        Waiting({composeTestRule.onNodeWithTag("active time").assertTextContains("$noveltIdo:3",true)},3999)
    }*/

    @Test
    fun sessionInfoPage(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        Waiting({composeTestRule.onNodeWithTag("create button tag").assertIsEnabled() })

        composeTestRule.onNodeWithTag("create button tag").performClick()

        Waiting({composeTestRule.onNodeWithTag("trade").assertIsDisplayed()})
        composeTestRule.onNodeWithTag("trade").performClick()
        composeTestRule.onNodeWithTag("market").performClick()
        composeTestRule.onNodeWithTag("limit").performClick()
        composeTestRule.onNodeWithTag("back").performClick()

        Waiting(
            { composeTestRule.onNodeWithTag("not yet opened").assertIsDisplayed()
                composeTestRule.onNodeWithTag("opened").assertIsDisplayed()
            })
    }

    @Test
    fun limitCard(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        Waiting({composeTestRule.onNodeWithTag("create button tag").assertIsEnabled()})

        composeTestRule.onNodeWithTag("create button tag").performClick()

        Waiting({composeTestRule.onNodeWithTag("trade").assertIsDisplayed()})
        composeTestRule.onNodeWithTag("trade").performClick()
        composeTestRule.onNodeWithTag("limit").performClick()
        Waiting({
            composeTestRule.onNodeWithTag("not yet opened").assertIsDisplayed()
            composeTestRule.onNodeWithTag("cancel").assertIsDisplayed()
        })

        composeTestRule.onNodeWithTag("cancel").performClick()

        Waiting({composeTestRule.onNodeWithTag("cancelled").assertIsDisplayed()})
    }

    @Test
    fun marketCard(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        Waiting({composeTestRule.onNodeWithTag("create button tag").assertIsEnabled()})

        composeTestRule.onNodeWithTag("create button tag").performClick()

        Waiting({composeTestRule.onNodeWithTag("trade").assertIsDisplayed()})
        composeTestRule.onNodeWithTag("trade").performClick()
        composeTestRule.onNodeWithTag("market").performClick()

        Waiting({
            composeTestRule.onNodeWithTag("opened").assertIsDisplayed()
            composeTestRule.onNodeWithTag("market close").assertIsDisplayed()
        })

        composeTestRule.onNodeWithTag("market close").performClick()

        Waiting({ composeTestRule.onNodeWithTag("closed").assertIsDisplayed()})
    }

    @Test
    fun invalidBep(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        Waiting({composeTestRule.onNodeWithTag("create button tag").assertIsEnabled()})

        composeTestRule.onNodeWithTag("create button tag").performClick()

        Waiting({composeTestRule.onNodeWithTag("trade").assertIsDisplayed()})
        composeTestRule.onNodeWithTag("trade").performClick()

        composeTestRule.onNodeWithTag("bep").performTextReplacement("éáőúűóüö")
        composeTestRule.onNodeWithTag("limit").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("market").assertIsEnabled()
        composeTestRule.onNodeWithTag("bep").performTextReplacement("123")
        composeTestRule.onNodeWithTag("limit").assertIsEnabled()
    }

    @Test
    fun invalidSep(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        Waiting({composeTestRule.onNodeWithTag("create button tag").assertIsEnabled()})

        composeTestRule.onNodeWithTag("create button tag").performClick()

        Waiting({composeTestRule.onNodeWithTag("trade").assertIsDisplayed()})
        composeTestRule.onNodeWithTag("trade").performClick()

        composeTestRule.onNodeWithTag("sep").performTextReplacement("éáőúűóüö")
        composeTestRule.onNodeWithTag("limit").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("market").assertIsNotEnabled()
        composeTestRule.onNodeWithTag("sep").performTextReplacement("123")
        composeTestRule.onNodeWithTag("limit").assertIsEnabled()
        composeTestRule.onNodeWithTag("market").assertIsEnabled()

    }

    @Test
    fun invalidTrail(){
        sHour=8
        eHour=16

        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        Waiting({composeTestRule.onNodeWithTag("create button tag").assertIsEnabled()})

        composeTestRule.onNodeWithTag("create button tag").performClick()

        Waiting({composeTestRule.onNodeWithTag("trade").assertIsDisplayed()})
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
    fun setTrail(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        Waiting({composeTestRule.onNodeWithTag("create button tag").assertIsEnabled()})

        composeTestRule.onNodeWithTag("create button tag").performClick()

        Waiting({composeTestRule.onNodeWithTag("trade").assertIsDisplayed()})
        composeTestRule.onNodeWithTag("trade").performClick()

        composeTestRule.onNodeWithTag("trailing stop").performClick()
        composeTestRule.onNodeWithTag("trail value").assertIsEnabled()
        composeTestRule.onNodeWithTag("trailing stop").performClick()
        composeTestRule.onNodeWithTag("trail value").assertIsNotEnabled()
    }

    @Test
    fun checkRandomButton(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        Waiting({composeTestRule.onNodeWithTag("create button tag").assertIsEnabled()})

        composeTestRule.onNodeWithTag("randomize").performClick()

        Waiting({composeTestRule.onNodeWithTag("create button tag").assertIsEnabled()})

        var tartalmazas=false
        try {
            composeTestRule.onNodeWithTag("first date")
                .assertTextContains(
                    SimpleDateFormat.getDateInstance()
                    .format(getUnixTsFromDate(sYear, sMonth, sDay)))
        }catch (e: AssertionError){
            tartalmazas=true
        }
        if(!tartalmazas){
            throw AssertionError("A dátum nem változott meg a randomizálás gombra")
        }


    }

    @Test
    fun createNewSessionSuccessfully(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        Waiting({composeTestRule.onNodeWithTag("create button tag").assertIsEnabled()})
    }

    @Test
    fun createNewSessionPlus1HourChecked(){
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        Waiting({composeTestRule.onNodeWithTag("create button tag").assertIsEnabled()})

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

        Waiting({composeTestRule.onNodeWithTag("trade").assertIsDisplayed()})
        composeTestRule.onNodeWithTag("trade").performClick()

        composeTestRule.onNodeWithTag("active time").assertTextContains("$noveltIdo:0",true)
    }

    @Test
    fun createNewSessionPlus1HourUnchecked() {
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        Waiting({composeTestRule.onNodeWithTag("create button tag").assertIsEnabled()})

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

        Waiting({composeTestRule.onNodeWithTag("trade").assertIsDisplayed()})
        composeTestRule.onNodeWithTag("trade").performClick()

        composeTestRule.onNodeWithTag("active time").assertTextContains("$ido:0",true)
    }

    @Test
    fun createNewSessionUnsuccessfully(){
        randomDate =false
        sYear =  2022
        sMonth =  5
        sDay =  16
        eYear = mutableStateOf( 2022)
        eMonth = mutableStateOf( 5)
        eDay = mutableStateOf( 16)

        composeTestRule.onNodeWithTag("new session tag").performClick()

        Waiting({composeTestRule.onNodeWithTag("not available").assertIsDisplayed()})

        randomDate =true

    }

    @Test
    fun deleteAccount(){
        composeTestRule.onNodeWithTag("account").performClick()
        composeTestRule.onNodeWithTag("delete account").performClick()
        composeTestRule.onNodeWithTag("sure to delete account").performClick()

        Waiting({composeTestRule.onNodeWithTag("no sessions").assertIsDisplayed()})

    }

    @Test
    fun changeLimit() {
        composeTestRule.onNodeWithTag("new session tag").performClick()
        composeTestRule.onNodeWithTag("create button tag").assertIsDisplayed()

        Waiting({composeTestRule.onNodeWithTag("create button tag").assertIsEnabled()})

        composeTestRule.onNodeWithTag("create button tag").performClick()

        Waiting({composeTestRule.onNodeWithTag("trade").assertIsDisplayed()})
        composeTestRule.onNodeWithTag("trade").performClick()
        composeTestRule.onNodeWithTag("bep").assertIsDisplayed()


        composeTestRule.onNodeWithTag("bep").performTextClearance()
        composeTestRule.onNodeWithTag("bep").performTextInput("1234")

        Waiting({composeTestRule.onNodeWithTag("limit").assertExists()})

        composeTestRule.onNodeWithTag("limit").assertTextEquals("Limit @ 1234")
    }

    @Test
    fun profileButton(){
        composeTestRule.onNodeWithTag("account").performClick()
        Waiting({composeTestRule.onNodeWithTag("loginButtonTag").assertIsDisplayed()})
    }

    @Test
    fun settingsButtonWorking(){
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
        /*composeTestRule.waitUntil(15000){
            componentWithTextLoaded("Session info")
        }*/
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
        /*composeTestRule.waitUntil(15000){
            componentWithTextLoaded("Session info")
        }*/
        Waiting({composeTestRule.onNodeWithText("Progress: 100%").assertExists()})

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        /*composeTestRule.waitUntil(15000){
            componentWithTextLoaded("Sessions")
        }*/
        Waiting({composeTestRule.onNodeWithText("Progress: 100%").assertExists()})


        eHour=16
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

    fun Waiting(ellenorzes:()-> SemanticsNodeInteraction, ido: Long =15000){
        composeTestRule.waitUntil(ido){
            try {
                ellenorzes()
            }catch (e: AssertionError){
                return@waitUntil false
            }
            return@waitUntil true
        }
    }
    
    
}