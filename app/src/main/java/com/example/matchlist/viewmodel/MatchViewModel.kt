package com.example.matchlist.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matchlist.API.RetrofitClient
import com.example.matchlist.FirestoreManager
import com.example.matchlist.ui.screens.Produto
import kotlinx.coroutines.launch

class MatchViewModel(
    private val firestoreManager: FirestoreManager,
    private val userUid: String
) : ViewModel() {

    var produtos    by mutableStateOf<List<Produto>>(emptyList())
    var indiceAtual by mutableIntStateOf(0)
    var status by mutableStateOf<MatchStatus>(MatchStatus.Loading)

    init {
        carregarDados()
    }

    private fun carregarDados() {
        viewModelScope.launch {
            status = MatchStatus.Loading
            try {
                val listaApi = RetrofitClient.instance.getEletronicos()
                produtos = listaApi.map {
                    Produto(
                        id        = it.id.toString(),
                        nome      = it.title,
                        preco     = "R$ ${it.price}",
                        imagemUrl = it.image
                    )
                }
                status = if (produtos.isEmpty()) MatchStatus.Empty else MatchStatus.Ready
            } catch (e: Exception) {
                status = MatchStatus.Error(e.message ?: "")
            }
        }
    }

    fun curtir() {
        val p = produtos.getOrNull(indiceAtual) ?: return
        firestoreManager.salvarNaWishlist(userUid, p.id, p.nome, p.preco) { _, _, _ -> }
        avancar()
    }

    fun pular() = avancar()

    private fun avancar() {
        if (indiceAtual < produtos.size) indiceAtual++
    }
}