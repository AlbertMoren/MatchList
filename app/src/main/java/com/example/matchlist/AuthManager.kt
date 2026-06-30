package com.example.matchlist

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore

class AuthManager(private val auth: FirebaseAuth, private val db: FirebaseFirestore) {
    fun cadastrarUsuario(nome: String, email: String, senha: String, onResult: (ok: Boolean, uid: String?, msgResId: Int, msgArg: String?) -> Unit) {
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
                            onResult(true, uid, 0, null)
                        }
                        .addOnFailureListener { e ->
                            onResult(false, null, R.string.auth_save_error, e.message)
                        }

                } else {
                    onResult(false, null, R.string.auth_register_unknown_error,task.exception?.message)
                }
            }
    }

    fun loginUsuario(email: String, senha: String, onResult: (ok: Boolean, uid: String?, msgResId: Int, msgArg: String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, senha)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, auth.currentUser?.uid ?: "", 0, null)
                } else {
                    onResult(false, null, R.string.auth_login_unknown_error,task.exception?.message)
                }
            }
    }

    fun resetarSenha(
        email: String,
        onResult: (ok: Boolean, msgResId: Int, msgArg: String?) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, R.string.auth_reset_email_sent, null)
                } else {
                    val resId = when (task.exception) {
                        is FirebaseAuthInvalidCredentialsException -> R.string.auth_reset_invalid_email
                        is FirebaseAuthInvalidUserException        -> R.string.auth_reset_user_not_found
                        else                                       -> R.string.auth_reset_unknown_error
                    }
                    onResult(false, resId, task.exception?.message)
                }
            }
    }

    fun deslogarUsuario() {
        auth.signOut()
    }
}