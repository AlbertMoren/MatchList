package com.example.matchlist

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CadastroTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testarFluxoDeCadastro() {
        // vai pra tela de cadastro
        onView(withId(R.id.btnCadastrar))
            .perform(click())

        // preeenche os campos
        onView(withId(R.id.editNomeCadastro))
            .perform(typeText("Test Lab"), closeSoftKeyboard())

        onView(withId(R.id.editEmailCadastro))
            .perform(typeText("test@email.com"), closeSoftKeyboard())

        onView(withId(R.id.editSenhaCadastro))
            .perform(typeText("senha123"), closeSoftKeyboard())

        // finaliza o cadastro
        onView(withId(R.id.btnFinalizarCadastro))
            .perform(click())

        // delay 3s
        Thread.sleep(3000)

        // confere se houve transição
        onView(withId(R.id.MatchScreen))
            .check(matches(isDisplayed()))
    }
}