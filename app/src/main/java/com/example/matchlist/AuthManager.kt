package com.example.matchlist

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthManager(private val auth: FirebaseAuth, private val db: FirebaseFirestore) {

    fun cadastrarUsuario(nome: String, email: String, senha: String, onResult: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""

                    val dadosUsuario = hashMapOf(
                        "nome" to nome,
                        "email" to email,
                        "role" to "user"
                    )

                    db.collection("users").document(uid).set(dadosUsuario)
                        .addOnSuccessListener {
                            onResult(true, uid)
                        }
                        .addOnFailureListener { e ->
                            onResult(false, "Erro ao salvar dados: ${e.message}")
                        }

                } else {
                    val erro = task.exception?.message ?: "Erro desconhecido ao cadastrar"
                    onResult(false, erro)
                }
            }
    }

    fun loginUsuario(email: String, senha: String, onResult: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    onResult(true, uid)
                } else {
                    val erro = task.exception?.message ?: "Erro desconhecido ao logar"
                    onResult(false, erro)
                }
            }
    }

    fun deslogarUsuario() {
        auth.signOut()
    }
}