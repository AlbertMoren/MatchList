package com.example.matchlist

import android.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

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

    fun registrarLog(acao: String, userId: String, produtoId: String) {
        val dadosLog = hashMapOf(
            "acao" to acao,
            "produtoId" to produtoId,
            "dataHora" to FieldValue.serverTimestamp()
        )

        db.collection("admin_logs")
            .document(userId)
            .collection("logs")
            .add(dadosLog)
            .addOnSuccessListener {
                print("Log gravado com sucesso na pasta do usuário: $acao")
            }
            .addOnFailureListener { e ->
                print("Erro ao gravar log: ${e.message}")
            }
    }

    // Nova função que filtra o que o usuário já viu
    fun buscarProdutosNaoVistos(userUid: String, onResult: (Boolean, List<Map<String, String>>) -> Unit) {

        //olhamos o histórico (logs) do usuário para saber o que ele já curtiu ou descurtiu
        db.collection("admin_logs")
            .document(userUid)
            .collection("logs")
            .get()
            .addOnSuccessListener { logs ->
                val produtosVistos = mutableListOf<String>()
                for (log in logs) {
                    val produtoId = log.getString("produtoId")
                    if (produtoId != null) {
                        produtosVistos.add(produtoId)
                    }
                }

                // 2. Agora buscamos TODOS os produtos da loja
                db.collection("produtos")
                    .get()
                    .addOnSuccessListener { documentos ->
                        val listaFinal = mutableListOf<Map<String, String>>()

                        for (documento in documentos) {
                            // Só entra na lista da tela se o ID NÃO estiver na lista de "vistos"
                            if (!produtosVistos.contains(documento.id)) {
                                val produto = mapOf(
                                    "id" to documento.id,
                                    "nome" to (documento.get("nome")?.toString() ?: ""),
                                    "preco" to (documento.get("preco")?.toString() ?: ""),
                                    "imagem" to (documento.get("imagem")?.toString() ?: "")
                                )
                                listaFinal.add(produto)
                            }
                        }

                        // Devolvemos apenas a lista filtrada para a tela
                        onResult(true, listaFinal)
                    }
                    .addOnFailureListener {
                        onResult(false, emptyList())
                    }
            }
            .addOnFailureListener {
                onResult(false, emptyList())
            }
    }
}