package com.example.matchlist

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.matchlist.ui.screens.CadastroScreen
import com.example.matchlist.ui.screens.LoginScreen
import com.example.matchlist.ui.screens.MainScreen
import com.example.matchlist.ui.theme.MatchListTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

object RootRoutes {
    const val LOGIN    = "login"
    const val CADASTRO = "cadastro"
    const val HOME     = "home/{userUid}"
    fun home(uid: String) = "home/$uid"
}

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        authManager = AuthManager(auth, FirebaseFirestore.getInstance())

        val prefs = getSharedPreferences("CONFIG_APP", Context.MODE_PRIVATE)

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember {
                mutableStateOf(prefs.getBoolean("MODO_ESCURO", systemDark))
            }

            MatchListTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val startDest = if (auth.currentUser != null)
                        RootRoutes.home(auth.currentUser!!.uid)
                    else
                        RootRoutes.LOGIN

                    NavHost(navController = navController, startDestination = startDest) {

                        // login
                        composable(RootRoutes.LOGIN) {
                            var resultado by remember { mutableStateOf("") }

                            LoginScreen(
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { dark ->
                                    isDarkTheme = dark
                                    prefs.edit().putBoolean("MODO_ESCURO", dark).apply()
                                },
                                onEntrar = { email, senha ->
                                    if (email.isNotEmpty() && senha.isNotEmpty()) {
                                        resultado = getString(R.string.login_loading)
                                        authManager.loginUsuario(email, senha) { ok, uid, resId, arg ->
                                            if (ok && uid != null) {
                                                navController.navigate(RootRoutes.home(uid)) {
                                                    popUpTo(RootRoutes.LOGIN) { inclusive = true }
                                                }
                                            } else {
                                                val msg = if (arg != null) getString(resId, arg)
                                                else getString(resId)
                                                resultado = getString(R.string.login_error_prefix, msg)
                                            }
                                        }
                                    } else {
                                        resultado = getString(R.string.login_fill_fields)
                                    }
                                },
                                onCadastrar = { navController.navigate(RootRoutes.CADASTRO) },
                                onEsqueciSenha = { email ->
                                    if (email.isNotEmpty()) {
                                        authManager.resetarSenha(email) { _, resId, arg ->
                                            resultado = if (arg != null) getString(resId, arg)
                                            else getString(resId)
                                        }
                                    } else {
                                        resultado = getString(R.string.login_forgot_fill_email)
                                    }
                                },
                                resultado = resultado
                            )
                        }

                        // cadastro
                        composable(RootRoutes.CADASTRO) {
                            var resultado by remember { mutableStateOf("") }

                            CadastroScreen(
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { dark ->
                                    isDarkTheme = dark
                                    prefs.edit().putBoolean("MODO_ESCURO", dark).apply()
                                },
                                onCadastrar = { nome, email, senha ->
                                    if (nome.isNotEmpty() && email.isNotEmpty() && senha.isNotEmpty()) {
                                        resultado = getString(R.string.cadastro_creating)
                                        authManager.cadastrarUsuario(nome, email, senha) { ok, uid, resId, arg ->
                                            if (ok && uid != null) {
                                                navController.navigate(RootRoutes.home(uid)) {
                                                    popUpTo(RootRoutes.LOGIN) { inclusive = true }
                                                }
                                            } else {
                                                val msg = if (arg != null) getString(resId, arg)
                                                else getString(resId)
                                                resultado = getString(R.string.cadastro_error_prefix, msg)
                                            }
                                        }
                                    } else {
                                        resultado = getString(R.string.cadastro_fill_fields)
                                    }
                                },
                                resultado = resultado
                            )
                        }

                        // home
                        composable(RootRoutes.HOME) { backStack ->
                            val userUid = backStack.arguments?.getString("userUid") ?: ""
                            MainScreen(
                                userUid = userUid,
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { dark ->
                                    isDarkTheme = dark
                                    prefs.edit().putBoolean("MODO_ESCURO", dark).apply()
                                },
                                onSair = {
                                    authManager.deslogarUsuario()
                                    navController.navigate(RootRoutes.LOGIN) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}