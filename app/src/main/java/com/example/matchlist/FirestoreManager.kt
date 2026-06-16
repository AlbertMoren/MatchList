package com.example.matchlist

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreManager(private val db: FirebaseFirestore) {

    // Função que recebe os dados e salva no banco do firebase:
    // users -> UID -> wishlist -> ID_DO_PRODUTO
    fun salvarNaWishlist(uid: String, produtoId: String, produtoNome: String, preco: String, onResult: (Boolean, String) -> Unit) {

        val dadosProduto = hashMapOf(
            "id" to produtoId,
            "nome" to produtoNome,
            "preco" to preco,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("users")
            .document(uid)
            .collection("wishlist")
            .document(produtoId)
            .set(dadosProduto)
            .addOnSuccessListener {
                onResult(true, "Salvo com sucesso na Wishlist!")
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "Erro desconhecido ao salvar")
            }
    }
}