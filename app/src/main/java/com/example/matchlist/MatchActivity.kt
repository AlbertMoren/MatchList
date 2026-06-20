package com.example.matchlist

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class MatchActivity : ComponentActivity() {

    private lateinit var btnLike: Button
    private lateinit var btnDislike: Button
    private lateinit var txtStatusMatch: TextView
    private lateinit var txtNomeProduto: TextView
    private lateinit var txtPrecoProduto: TextView

    private var userUid: String = ""

    private var listaProdutos: List<Map<String, String>> = emptyList()
    private var indiceAtual = 0

    private lateinit var db: FirebaseFirestore
    private lateinit var firestoreManager: FirestoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)

        userUid = intent.getStringExtra("USER_UID") ?: ""

        db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestoreManager = FirestoreManager(db)

        btnLike = findViewById(R.id.btnLike)
        btnDislike = findViewById(R.id.btnDislike)
        txtStatusMatch = findViewById(R.id.txtStatusMatch)
        txtNomeProduto = findViewById(R.id.txtNomeProduto)
        txtPrecoProduto = findViewById(R.id.txtPrecoProduto)

        txtStatusMatch.text = "Carregando vitrine..."

        firestoreManager.BuscarTodosOsProdutos { sucesso, produtos ->
            if (sucesso) {
                if (produtos.isNotEmpty()) {
                    listaProdutos = produtos
                    indiceAtual = 0
                    exibirProdutoAtual()
                    txtStatusMatch.text = "Produtos carregados!"
                } else {
                    txtStatusMatch.text = "Erro: Banco acessado, mas a coleção está vazia (nomes diferentes?)."
                    desativarBotoes()
                }
            } else {
                txtStatusMatch.text = "Erro: Permissão negada ou sem internet. Faça o login novamente!"
                desativarBotoes()
            }
        }

        btnLike.setOnClickListener {
            if (indiceAtual < listaProdutos.size) {
                val produto = listaProdutos[indiceAtual]
                val id = produto["id"] ?: ""
                val nome = produto["nome"] ?: ""
                val preco = produto["preco"] ?: ""

                txtStatusMatch.text = "Salvando..."

                firestoreManager.salvarNaWishlist(userUid, id, nome, preco) { sucesso, msg ->
                    if (sucesso) {
                        txtStatusMatch.text = "⭐ Salvo! Indo para o próximo..."
                        firestoreManager.registrarLog("NOVO_LIKE", userUid, id)
                        proximoProduto()
                    } else {
                        txtStatusMatch.text = "❌ Erro ao salvar: $msg"
                    }
                }
            }
        }

        btnDislike.setOnClickListener {
            if (indiceAtual < listaProdutos.size) {
                val produto = listaProdutos[indiceAtual]
                val id = produto["id"] ?: ""

                firestoreManager.registrarLog("NOVO_DISLIKE", userUid, id)
            }

            txtStatusMatch.text = "❌ Pulou!"
            proximoProduto()
        }
    }

    private fun exibirProdutoAtual() {
        if (indiceAtual < listaProdutos.size) {
            val produto = listaProdutos[indiceAtual]
            txtNomeProduto.text = produto["nome"]
            txtPrecoProduto.text = "R$ ${produto["preco"]}"
        } else {
            txtNomeProduto.text = "Fim da fila!"
            txtPrecoProduto.text = "Você já viu tudo."
            txtStatusMatch.text = ""
            desativarBotoes()
        }
    }

    private fun proximoProduto() {
        indiceAtual++
        exibirProdutoAtual()
    }

    private fun desativarBotoes() {
        btnLike.visibility = View.GONE
        btnDislike.visibility = View.GONE
    }
}