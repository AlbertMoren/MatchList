package com.example.matchlist

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class WishlistActivity : ComponentActivity() {

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        // Pega o ID do usuário
        val userUid = intent.getStringExtra("USER_UID") ?: ""
        db = FirebaseFirestore.getInstance()

        val listView = findViewById<ListView>(R.id.listViewWishlist)
        val btnVoltar = findViewById<Button>(R.id.btnVoltarMatch)

        // Botão para fechar a tela e voltar
        btnVoltar.setOnClickListener {
            finish()
        }

        // Vai direto no banco buscar os itens na pasta "wishlist" do usuário
        db.collection("users")
            .document(userUid)
            .collection("wishlist")
            .get()
            .addOnSuccessListener { documentos ->
                val listaExibicao = mutableListOf<String>()

                // Pega cada documento que voltou e monta um texto bonitinho
                for (doc in documentos) {
                    val nome = doc.getString("nome") ?: "Produto sem nome"
                    val preco = doc.getString("preco") ?: "0.00"

                    listaExibicao.add("$nome\nR$ $preco")
                }

                if (listaExibicao.isEmpty()) {
                    listaExibicao.add("A sua wishlist está vazia! Dê uns likes primeiro.")
                }


                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaExibicao)
                listView.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao buscar a wishlist", Toast.LENGTH_SHORT).show()
            }
    }
}