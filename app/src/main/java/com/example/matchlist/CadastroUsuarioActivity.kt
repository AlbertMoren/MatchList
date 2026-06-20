package com.example.matchlist

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CadastroUsuarioActivity : ComponentActivity() {

    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cadastro_usuario)

        val auth = Firebase.auth
        val db = Firebase.firestore
        authManager = AuthManager(auth, db)

        val editNome = findViewById<EditText>(R.id.editNomeCadastro)
        val editEmail = findViewById<EditText>(R.id.editEmailCadastro)
        val editSenha = findViewById<EditText>(R.id.editSenhaCadastro)
        val btnFinalizar = findViewById<Button>(R.id.btnFinalizarCadastro)
        val txtResultado = findViewById<TextView>(R.id.txtResultadoCadastro)

        btnFinalizar.setOnClickListener {
            val nome = editNome.text.toString()
            val email = editEmail.text.toString()
            val senha = editSenha.text.toString()

            if (nome.isNotEmpty() && email.isNotEmpty() && senha.isNotEmpty()) {
                txtResultado.text = "Criando conta..."


                authManager.cadastrarUsuario(nome, email, senha) { sucesso, resposta ->
                    if (sucesso) {
                        val intent = Intent(this, MatchActivity::class.java)
                        intent.putExtra("USER_UID", resposta)
                        startActivity(intent)
                        finish()
                    } else {
                        txtResultado.text = "❌ Erro: $resposta"
                    }
                }
            } else {
                txtResultado.text = "Preencha todos os campos!"
            }
        }
    }
}