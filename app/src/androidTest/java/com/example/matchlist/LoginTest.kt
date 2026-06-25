package com.example.matchlist

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testarLoginLikeEWishlist() {
        // faz login
        onView(withId(R.id.editEmail))
            .perform(typeText("teste@email.com"), closeSoftKeyboard())

        onView(withId(R.id.editSenha))
            .perform(typeText("senha123"), closeSoftKeyboard())

        onView(withId(R.id.btnEntrar))
            .perform(click())

        // delay pra esperar a conexao com firebase/carregar imagens
        Thread.sleep(8000)

        // curte um produto
        onView(withId(R.id.btnLike))
            .perform(click())

        // delay salva produto
        Thread.sleep(4000)

        // vai pra wishlist
        onView(withId(R.id.bntVerWishList))
            .perform(click())

        // delay pra tela carregar
        Thread.sleep(2000)
    }
}