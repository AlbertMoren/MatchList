package com.example.matchlist

import com.google.firebase.firestore.FirebaseFirestore

class FirestoreManager(private val db: FirebaseFirestore) {

    fun salvarNaWishlist(uid: String, produtoId: String, produtoNome: String, preco: String, onResult: (ok: Boolean, msgResId: Int, msgArg: String?) -> Unit) {
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
                onResult(true, R.string.firestore_save_success, null)
            }
            .addOnFailureListener { e ->
                onResult(false, R.string.firestore_save_error, e.message)
            }
    }
    fun buscarWishlist(userUid: String, callback: (List<Map<String, Any>>) -> Unit) {
        db.collection("users").document(userUid)
            .collection("wishlist").get()
            .addOnSuccessListener { snap ->
                val lista = snap.documents.map { doc ->
                    val data = doc.data?.toMutableMap() ?: mutableMapOf()
                    data["id"] = doc.getString("id") ?: doc.getString("idProduto") ?: doc.id
                    data
                }
                callback(lista)
            }
            .addOnFailureListener { callback(emptyList()) }
    }

    // Função que registra os logs no banco de dados
    fun registrarLog(tipo: String, userUid: String, idProduto: String) {
        val log = hashMapOf(
            "idProduto" to idProduto,
            "tipo" to tipo,
            "timestamp" to System.currentTimeMillis()
        )

        // Caminho: admin_logs / userUid / logs / (auto-id)
        db.collection("admin_logs")
            .document(userUid)
            .collection("logs")
            .add(log)
    }

    fun buscarTodosLogs(userUid: String, callback: (List<Map<String, Any>>) -> Unit) {
        db.collection("admin_logs")
            .document(userUid)
            .collection("logs")
            .get()
            .addOnSuccessListener { snapshot ->
                val logs = snapshot.documents.map { it.data as Map<String, Any> }
                callback(logs)
            }
            .addOnFailureListener {
                callback(emptyList()) // Retorna vazio se der erro
            }
    }

    // CORRIGIDO: Agora acessa os caminhos corretos da sua estrutura
    fun removerDaWishlistEResetarLog(userUid: String, idProduto: String, callback: (Boolean) -> Unit) {
        // Passo 1: Deletar direto da wishlist do usuário
        db.collection("users")
            .document(userUid)
            .collection("wishlist")
            .document(idProduto) // Usamos o ID do produto direto como ID do documento
            .delete()
            .addOnSuccessListener {

                // Passo 2: Procurar e deletar o log na subcoleção de logs do usuário
                db.collection("admin_logs")
                    .document(userUid)
                    .collection("logs")
                    .whereEqualTo("idProduto", idProduto)
                    .get()
                    .addOnSuccessListener { logsSnapshot ->
                        for (logDoc in logsSnapshot.documents) {
                            logDoc.reference.delete()
                        }
                        // Sucesso total
                        callback(true)
                    }
                    .addOnFailureListener {
                        callback(false)
                    }
            }
            .addOnFailureListener {
                callback(false)
            }
    }
}