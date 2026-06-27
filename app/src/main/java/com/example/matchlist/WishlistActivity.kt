package com.example.matchlist

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class WishlistActivity : BaseActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var firestoreManager: FirestoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        val userUid = intent.getStringExtra("USER_UID") ?: ""
        db = FirebaseFirestore.getInstance()
        firestoreManager = FirestoreManager(db)

        val listView = findViewById<ListView>(R.id.listViewWishlist)
        val btnVoltar = findViewById<Button>(R.id.btnVoltarMatch)

        btnVoltar.setOnClickListener {
            finish()
        }

        // Busca os itens na subcoleção correta
        db.collection("users")
            .document(userUid)
            .collection("wishlist")
            .get()
            .addOnSuccessListener { documentos ->
                val listaExibicao = mutableListOf<String>()
                val listaDocIds = mutableListOf<String>()     // Guarda o ID do documento no Firebase
                val listaProdutoIds = mutableListOf<String>() // Guarda o ID real do produto (ex: API_1)

                for (doc in documentos) {
                    val nome = doc.getString("nome") ?: "Produto sem nome"
                    val preco = doc.getString("preco") ?: "0.00"

                    // Tenta buscar o ID real de dentro do documento, se não achar usa o doc.id
                    val idProdutoReal = doc.getString("id") ?: doc.getString("idProduto") ?: doc.id

                    listaExibicao.add("$nome\nR$ $preco")
                    listaDocIds.add(doc.id)
                    listaProdutoIds.add(idProdutoReal)
                }

                if (listaExibicao.isEmpty()) {
                    listaExibicao.add("A sua wishlist está vazia! Dê uns likes primeiro.")
                }

                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaExibicao)
                listView.adapter = adapter

                // --- Clique Longo para deletar ---
                listView.setOnItemLongClickListener { _, _, position, _ ->

                    if (listaDocIds.isNotEmpty() && position < listaDocIds.size) {

                        // Pegamos o ID do produto que precisamos deletar
                        val produtoIdParaDeletar = listaProdutoIds[position]

                        // Aqui nós FINALMENTE chamamos a função que estava cinza!
                        firestoreManager.removerDaWishlistEResetarLog(userUid, produtoIdParaDeletar) { sucesso ->
                            if (sucesso) {
                                Toast.makeText(this, "Produto removido!", Toast.LENGTH_SHORT).show()

                                // Remove da tela imediatamente
                                listaExibicao.removeAt(position)
                                listaDocIds.removeAt(position)
                                listaProdutoIds.removeAt(position)

                                // Redesenha a lista
                                adapter.notifyDataSetChanged()
                            } else {
                                Toast.makeText(this, "Erro ao remover do banco", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    true
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao buscar a wishlist", Toast.LENGTH_SHORT).show()
            }
    }
}