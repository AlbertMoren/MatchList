package com.example.matchlist

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class MatchActivity : BaseActivity() {

    private lateinit var btnLike: FrameLayout
    private lateinit var btnDislike: FrameLayout
    private lateinit var txtStatusMatch: TextView
    private lateinit var txtNomeProduto: TextView
    private lateinit var txtPrecoProduto: TextView
    private lateinit var imgProduto: ImageView // Restaurado

    private var userUid: String = ""

    private var listaProdutos: List<Map<String, String>> = emptyList()
    private var indiceAtual = 0

    private lateinit var db: FirebaseFirestore
    private lateinit var firestoreManager: FirestoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)

        userUid = intent.getStringExtra("USER_UID") ?: ""

        db = FirebaseFirestore.getInstance()
        firestoreManager = FirestoreManager(db)

        btnLike = findViewById(R.id.btnLike)
        btnDislike = findViewById(R.id.btnDislike)
        txtStatusMatch = findViewById(R.id.txtStatusMatch)
        txtNomeProduto = findViewById(R.id.txtNomeProduto)
        txtPrecoProduto = findViewById(R.id.txtPrecoProduto)
        imgProduto = findViewById(R.id.imgProduto) // Restaurado

        val cardProduto = findViewById<View>(R.id.cardProduto)
        configurarGestoDeArrastar(cardProduto)

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

        val btnVerWishlist = findViewById<Button>(R.id.bntVerWishList)
        btnVerWishlist.setOnClickListener {
            val intent = android.content.Intent(this, WishlistActivity::class.java)
            intent.putExtra("USER_UID", userUid)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        carregarVitrine()
    }

    private fun carregarVitrine() {
        txtStatusMatch.text = "Carregando vitrine..."

        btnLike.visibility = View.VISIBLE
        btnDislike.visibility = View.VISIBLE
        imgProduto.visibility = View.VISIBLE // Garante que a imagem volta a aparecer

        firestoreManager.buscarProdutosNaoVistos(userUid) { sucesso, produtos ->
            if (sucesso) {
                if (produtos.isNotEmpty()) {
                    listaProdutos = produtos
                    indiceAtual = 0
                    exibirProdutoAtual()
                    txtStatusMatch.text = "Produtos carregados!"
                } else {
                    txtStatusMatch.text = "Você já viu todos os produtos do estoque!"
                    txtNomeProduto.text = "Fim da fila!"
                    txtPrecoProduto.text = "Volte mais tarde."
                    imgProduto.visibility = View.GONE // Esconde a imagem no fim da fila
                    desativarBotoes()
                }
            } else {
                txtStatusMatch.text = "Erro: Permissão negada ou sem internet."
                desativarBotoes()
            }
        }
    }

    private fun exibirProdutoAtual() {
        if (indiceAtual < listaProdutos.size) {
            val produto = listaProdutos[indiceAtual]
            txtNomeProduto.text = produto["nome"]
            txtPrecoProduto.text = "R$ ${produto["preco"]}"

            // O Glide pega a URL do Firebase e joga na tela (Restaurado)
            val urlImagem = produto["imagem"] ?: ""
            if (urlImagem.isNotEmpty()) {
                Glide.with(this).load(urlImagem).into(imgProduto)
            } else {
                imgProduto.setImageResource(android.R.color.transparent)
            }

        } else {
            txtNomeProduto.text = "Fim da fila!"
            txtPrecoProduto.text = "Você já viu tudo."
            txtStatusMatch.text = ""
            imgProduto.visibility = View.GONE
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

    // --- NOVO: gesto de arrastar o card (swipe), sem nenhuma biblioteca externa ---

    private fun configurarGestoDeArrastar(card: View) {
        var dX = 0f
        var dY = 0f

        card.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    dX = view.translationX - event.rawX
                    dY = view.translationY - event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    view.translationX = event.rawX + dX
                    view.translationY = event.rawY + dY
                    view.rotation = (view.translationX / 20f).coerceIn(-15f, 15f)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val limite = view.width * 0.35f
                    when {
                        view.translationX > limite -> animarSaida(view, direita = true)
                        view.translationX < -limite -> animarSaida(view, direita = false)
                        else -> animarRetornoAoCentro(view)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun animarSaida(view: View, direita: Boolean) {
        val destinoX = if (direita) 1200f else -1200f
        view.animate()
            .translationX(destinoX)
            .rotation(if (direita) 25f else -25f)
            .setDuration(250)
            .withEndAction {
                // Dispara exatamente o mesmo código que já existia nos botões —
                // nenhuma lógica de like/dislike/imagem foi duplicada ou alterada.
                if (direita) btnLike.performClick() else btnDislike.performClick()

                view.translationX = 0f
                view.translationY = 0f
                view.rotation = 0f
            }
            .start()
    }

    private fun animarRetornoAoCentro(view: View) {
        view.animate()
            .translationX(0f)
            .translationY(0f)
            .rotation(0f)
            .setDuration(200)
            .start()
    }
}