package com.example.matchlist

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class MainActivity : ComponentActivity() {

    //backend
    private lateinit var auth: FirebaseAuth
    private lateinit var authManager: AuthManager

    //elementos de tela (XML)
    private lateinit var editEmail: EditText
    private lateinit var editSenha: EditText
    private lateinit var btnCadastrar: Button
    private lateinit var btnEntrar: Button
    private lateinit var txtResultado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        authManager = AuthManager(auth,db)

        mapearComponentesXml()

        configurarBotoes()
    }

    private fun mapearComponentesXml() {
        editEmail = findViewById(R.id.editEmail)
        editSenha = findViewById(R.id.editSenha)
        btnCadastrar = findViewById(R.id.btnCadastrar)
        btnEntrar = findViewById(R.id.btnEntrar)
        txtResultado = findViewById(R.id.txtResultado)
    }

    private fun configurarBotoes() {

        btnCadastrar.setOnClickListener {
            val intent = android.content.Intent(this, CadastroUsuarioActivity::class.java)
            startActivity(intent)
        }

        btnEntrar.setOnClickListener {
            val email = editEmail.text.toString()
            val senha = editSenha.text.toString()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                txtResultado.text = "Carregando..."

                authManager.loginUsuario(email, senha) { sucesso, resposta ->
                    if (sucesso) {
                        val intent = android.content.Intent(this, MatchActivity::class.java)
                        intent.putExtra("USER_UID",resposta)
                        startActivity(intent)
                        finish()
                    } else {
                        txtResultado.text = "❌ Falha no login: $resposta"
                    }
                }
            } else {
                txtResultado.text = "Preencha e-mail e senha!"
            }
        }
    }
}