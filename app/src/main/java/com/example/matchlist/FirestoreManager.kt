package com.example.matchlist

import android.R
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreManager(private val db: FirebaseFirestore) {
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
    fun BuscarTodosOsProdutos(onResult: (Boolean, List<Map<String,String>>) -> Unit){
        db.collection("produtos")
            .get()
            .addOnSuccessListener { documentos ->
                val lista = mutableListOf<Map<String,String>>()

                for (documento in documentos){
                    val produto = mapOf(
                        "id" to documento.id,
                        "nome" to (documento.getString("nome") ?: ""),
                        "preco" to (documento.get("preco")?.toString() ?: "")
                    )
                    lista.add(produto)
                }

                onResult(true,lista)
            }
            .addOnFailureListener {
                onResult(false,emptyList())
            }
    }
}