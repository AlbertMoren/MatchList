package com.example.matchlist

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore

class WishlistActivity : BaseActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var firestoreManager: FirestoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wishlist)

        // Pega o ID do usuário que passamos pela Intent
        val userUid = intent.getStringExtra("USER_UID") ?: ""
        db = FirebaseFirestore.getInstance()
        firestoreManager = FirestoreManager(db)

        val listView = findViewById<ListView>(R.id.listViewWishlist)
        val btnVoltar = findViewById<Button>(R.id.btnVoltarMatch)

        // Botão para fechar a tela e voltar para os matches
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
                val listaIds = mutableListOf<String>()

                // Pega cada documento que voltou e monta um texto bonitinho
                for (doc in documentos) {
                    val nome = doc.getString("nome") ?: "Produto sem nome"
                    val preco = doc.getString("preco") ?: "0.00"

                    listaExibicao.add("$nome\nR$ $preco")
                    listaIds.add(doc.id)
                }

                if (listaExibicao.isEmpty()) {
                    listaExibicao.add("A sua wishlist está vazia! Dê uns likes primeiro.")
                }

                // leva a lista de textos para a ListView da tela
                val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, listaExibicao)
                listView.adapter = adapter

                // --- NOVA FUNCIONALIDADE: Clique Longo para deletar ---
                listView.setOnItemLongClickListener { _, _, position, _ ->

                    // para não tentar deletar a mensagem de "lista vazia"
                    if (listaIds.isNotEmpty() && position < listaIds.size) {

                        // Pegamos o ID
                        val docIdParaDeletar = listaIds[position]

                        // 1. Vai no Firebase e apaga o documento físico da wishlist
                        db.collection("users")
                            .document(userUid)
                            .collection("wishlist")
                            .document(docIdParaDeletar)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(this, "Produto removido!", Toast.LENGTH_SHORT).show()

                                // --- ROTINA DE AMNÉSIA: Apaga o histórico de logs deste produto ---
                                db.collection("admin_logs")
                                    .document(userUid)
                                    .collection("logs")
                                    .whereEqualTo("produtoId", docIdParaDeletar)
                                    .get()
                                    .addOnSuccessListener { logsEncontrados ->
                                        // Passa por todos os logs antigos desse produto (ex: o NOVO_LIKE) e deleta
                                        for (log in logsEncontrados) {
                                            log.reference.delete()
                                        }
                                    }
                                // ------------------------------------------------------------------

                                // Remove da tela
                                listaExibicao.removeAt(position)
                                listaIds.removeAt(position)

                                // redesenha a lista
                                adapter.notifyDataSetChanged()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Erro ao remover do banco", Toast.LENGTH_SHORT).show()
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