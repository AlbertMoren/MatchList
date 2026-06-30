package com.example.matchlist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matchlist.R
import com.example.matchlist.ui.theme.MatchListTheme

@Composable
fun WishlistScreen(
    itens: List<Produto> = emptyList(),
    onVoltarMatch: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.wishlist_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        if (itens.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.wishlist_empty), textAlign = TextAlign.Center)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(itens) { produto ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = produto.nome,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = produto.preco,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Button(
            onClick = onVoltarMatch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 48.dp)
                .height(48.dp)
        ) {
            Text(stringResource(R.string.wishlist_back_button))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun WishlistScreenPreview() {
    MatchListTheme {
        WishlistScreen(
            itens = listOf(
                Produto("1", "Tênis Esportivo", "R$ 299,90"),
                Produto("2", "Camiseta Oversized", "R$ 89,90"),
                Produto("3", "Mochila Urbana", "R$ 199,00")
            )
        )
    }
}