package com.example.matchlist.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.matchlist.FirestoreManager
import com.example.matchlist.ui.screens.Produto

class WishlistViewModel(
    private val firestoreManager: FirestoreManager,
    private val userUid: String
) : ViewModel() {

    var itensWishlist by mutableStateOf<List<Produto>>(emptyList())

    init {
        carregarWishlist()
    }

    private fun carregarWishlist() {
        firestoreManager.buscarWishlist(userUid) { listaFirebase ->
            itensWishlist = listaFirebase.map { map ->
                Produto(
                    id    = map["id"].toString(),
                    nome  = map["nome"]?.toString().takeUnless { it.isNullOrBlank() } ?: "",
                    preco = map["preco"]?.toString() ?: "0.00"
                )
            }
        }
    }
}