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

    // Nossas instâncias de backend
    private lateinit var auth: FirebaseAuth
    private lateinit var authManager: AuthManager

    // Nossos elementos de tela (XML)
    private lateinit var editEmail: EditText
    private lateinit var editSenha: EditText
    private lateinit var btnCadastrar: Button
    private lateinit var btnEntrar: Button
    private lateinit var txtResultado: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Conecta esta Activity ao nosso arquivo XML
        setContentView(R.layout.activity_main)

        // Inicializa o Firebase e a nossa classe de gerenciamento
        auth = Firebase.auth
        authManager = AuthManager(auth)

        // Mapeia os elementos do XML para as variáveis do Kotlin
        mapearComponentesXml()

        // Configura as ações dos botões
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
            val email = editEmail.text.toString()
            val senha = editSenha.text.toString()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                txtResultado.text = "Carregando..."

                // Delega a responsabilidade para o AuthManager
                authManager.cadastrarUsuario(email, senha) { sucesso, resposta ->
                    if (sucesso) {
                        txtResultado.text = "✅ Cadastrado! UID: $resposta"
                    } else {
                        txtResultado.text = "❌ Erro: $resposta"
                    }
                }
            } else {
                txtResultado.text = "Preencha e-mail e senha!"
            }
        }

        btnEntrar.setOnClickListener {
            val email = editEmail.text.toString()
            val senha = editSenha.text.toString()

            if (email.isNotEmpty() && senha.isNotEmpty()) {
                txtResultado.text = "Carregando..."

                // Delega a responsabilidade para o AuthManager
                authManager.loginUsuario(email, senha) { sucesso, resposta ->
                    if (sucesso) {
                        txtResultado.text = "✅ Logado! UID: $resposta"
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