package com.example.matchlist

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MatchActivity : ComponentActivity() {

    private lateinit var btnLike: Button
    private lateinit var btnDislike: Button
    private lateinit var txtStatusMatch: TextView
    private var userUid: String = ""
    private lateinit var db: FirebaseFirestore
    private lateinit var firestoreManager: FirestoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)

        userUid = intent.getStringExtra("USER_UID") ?: ""
        db = Firebase.firestore
        firestoreManager = FirestoreManager(db)

        btnLike = findViewById(R.id.btnLike)
        btnDislike = findViewById(R.id.btnDislike)
        txtStatusMatch = findViewById(R.id.txtStatusMatch)

        txtStatusMatch.text = "Bem-vindo! Seu UID: $userUid"

        // Valos mocados para teste(Remover isso depois)
        val produtoIdFake = "prod_999"
        val produtoNomeFake = "Fone de Ouvido Pro"
        val precoFake = "299.90"

        btnLike.setOnClickListener {
            txtStatusMatch.text = "Salvando no banco de dados..."
            firestoreManager.salvarNaWishlist(userUid,produtoIdFake,produtoNomeFake,precoFake){
                sucesso, mensagem ->
                if (sucesso){
                    txtStatusMatch.text = "⭐ $mensagem"
                }else{
                    txtStatusMatch.text = "❌ $mensagem"
                }
            }
        }

        btnDislike.setOnClickListener {
            txtStatusMatch.text = "Clicou em DISLIKE! Pulando produto..."
        }
    }
}