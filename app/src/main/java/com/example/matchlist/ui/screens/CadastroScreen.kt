package com.example.matchlist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matchlist.R
import com.example.matchlist.ui.theme.MatchListTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroScreen(
    isDarkTheme: Boolean = false,
    onToggleTheme: (Boolean) -> Unit = {},
    onCadastrar: (nome: String, email: String, senha: String) -> Unit = { _, _, _ -> },
    resultado: String = ""
) {
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var senhaVisivel by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        val segmentedColors = SegmentedButtonDefaults.colors(
            activeContentColor    = MaterialTheme.colorScheme.primary,
            inactiveContentColor  = MaterialTheme.colorScheme.primary,
            activeContainerColor  = Color.Transparent,
            inactiveContainerColor = Color.Transparent,
            activeBorderColor     = MaterialTheme.colorScheme.primary,
            inactiveBorderColor   = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        SingleChoiceSegmentedButtonRow(modifier = Modifier.align(Alignment.TopEnd)) {
            SegmentedButton(
                selected = !isDarkTheme,
                onClick  = { onToggleTheme(false) },
                shape    = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                colors   = segmentedColors
            ) { Icon(Icons.Default.LightMode, contentDescription = stringResource(R.string.theme_light)) }
            SegmentedButton(
                selected = isDarkTheme,
                onClick  = { onToggleTheme(true) },
                shape    = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                colors   = segmentedColors
            ) {
                Icon(
                    painterResource(R.drawable.ic_lua),
                    contentDescription = stringResource(R.string.theme_dark),
                    modifier = Modifier.scale(scaleX = -1f, scaleY = 1f)
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.cadastro_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text(stringResource(R.string.cadastro_name_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.login_email_label)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = senha,
                onValueChange = { senha = it },
                label = { Text(stringResource(R.string.cadastro_password_label)) },
                singleLine = true,
                visualTransformation = if (senhaVisivel) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { senhaVisivel = !senhaVisivel }) {
                        Icon(
                            imageVector = if (senhaVisivel) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )

            Button(
                onClick = { onCadastrar(nome, email, senha) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.cadastro_button))
            }

            if (resultado.isNotEmpty()) {
                Text(text = resultado, fontSize = 16.sp, modifier = Modifier.padding(top = 24.dp))
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun CadastroScreenPreview() {
    MatchListTheme { CadastroScreen() }
}