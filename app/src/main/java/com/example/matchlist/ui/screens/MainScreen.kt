package com.example.matchlist.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.matchlist.FirestoreManager
import com.example.matchlist.R
import com.example.matchlist.viewmodel.MatchViewModel
import com.example.matchlist.viewmodel.WishlistViewModel
import com.google.firebase.firestore.FirebaseFirestore

private const val ROTA_MATCH    = "match"
private const val ROTA_WISHLIST = "wishlist"
private const val ROTA_PERFIL   = "perfil"

@Composable
fun MainScreen(
    userUid: String,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onSair: () -> Unit
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    selected = currentRoute == ROTA_MATCH,
                    onClick  = { navController.navegarParaAba(ROTA_MATCH) },
                    label    = { Text(stringResource(R.string.nav_match)) },
                    icon     = { Icon(Icons.Default.Home, contentDescription = stringResource(R.string.nav_match)) }
                )
                NavigationBarItem(
                    selected = currentRoute == ROTA_WISHLIST,
                    onClick  = { navController.navegarParaAba(ROTA_WISHLIST) },
                    label    = { Text(stringResource(R.string.nav_wishlist)) },
                    icon     = { Icon(Icons.Default.Favorite, contentDescription = stringResource(R.string.nav_wishlist)) }
                )
                NavigationBarItem(
                    selected = currentRoute == ROTA_PERFIL,
                    onClick  = { navController.navegarParaAba(ROTA_PERFIL) },
                    label    = { Text(stringResource(R.string.nav_profile)) },
                    icon     = { Icon(Icons.Default.Person, contentDescription = stringResource(R.string.nav_profile)) }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = ROTA_MATCH,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(ROTA_MATCH) {
                val factory = remember(userUid) {
                    object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return MatchViewModel(
                                FirestoreManager(FirebaseFirestore.getInstance()),
                                userUid
                            ) as T
                        }
                    }
                }
                val viewModel: MatchViewModel = viewModel(factory = factory)
                MatchScreen(viewModel = viewModel)
            }

            composable(ROTA_WISHLIST) {
                val factory = remember(userUid) {
                    object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return WishlistViewModel(
                                FirestoreManager(FirebaseFirestore.getInstance()),
                                userUid
                            ) as T
                        }
                    }
                }
                val viewModel: WishlistViewModel = viewModel(factory = factory)
                WishlistScreen(
                    itens = viewModel.itensWishlist,
                    onVoltarMatch = { navController.navegarParaAba(ROTA_MATCH) }
                )
            }

            composable(ROTA_PERFIL) {
                PerfilScreen(
                    userUid = userUid,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    onSair = onSair
                )
            }
        }
    }
}

private fun NavController.navegarParaAba(rota: String) {
    navigate(rota) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}