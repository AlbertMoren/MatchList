package com.example.matchlist

import com.google.firebase.auth.FirebaseAuth

//Classe responsavel por gerenciar a autenticação, cadastro e deslogar o usuario.
class AuthManager(private val auth: FirebaseAuth) {

    // Função que recebe email, senha e um Callback (uma função de retorno)
    // O Callback retorna um Boolean (Sucesso ou Falha) e uma String (O UID ou a mensagem de erro)
    fun cadastrarUsuario(email: String, senha: String, onResult: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: ""
                    onResult(true, uid)
                } else {
                    val erro = task.exception?.message ?: "Erro desconhecido ao cadastrar"
                    onResult(false, erro)
                }
            }
    }

    // Função que recebe email, senha e um Callback (uma função de retorno)
    // O Callback retorna um Boolean (Sucesso ou Falha) e uma String (O UID ou a mensagem de erro)
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