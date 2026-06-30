package com.example.matchlist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matchlist.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    userUid: String,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onSair: () -> Unit
) {
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { Firebase.auth }

    // campos editaveis dos dados do usuario
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var rua by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var cep by remember { mutableStateOf("") }

    // Estados para mensagens de salvar (0 = nada, 1 = sucesso, 2 = erro)
    var statusSalvar by remember { mutableIntStateOf(0) }
    var detalheErroSalvar by remember { mutableStateOf("") }

    var carregando by remember { mutableStateOf(true) }

    // Estados para alertas e exclusão de conta
    var mostrarDialogDeletar by remember { mutableStateOf(false) }
    var senhaConfirmacao by remember { mutableStateOf("") }

    // 0 = sem erro, 1 = senha incorreta, 2 = erro generico
    var tipoErroDeletar by remember { mutableIntStateOf(0) }
    var detalheErroDeletar by remember { mutableStateOf("") }

    // carrega Firestore ao abrir
    LaunchedEffect(userUid) {
        db.collection("users").document(userUid).get()
            .addOnSuccessListener { doc ->
                nome = doc.getString("nome") ?: ""
                rua = doc.getString("rua") ?: ""
                numero = doc.getString("numero") ?: ""
                bairro = doc.getString("bairro") ?: ""
                cidade = doc.getString("cidade") ?: ""
                estado = doc.getString("estado") ?: ""
                cep = doc.getString("cep") ?: ""
                carregando = false
            }
            .addOnFailureListener { carregando = false }
    }

    if (carregando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // alerta de confirmação de exclusão de conta
    if (mostrarDialogDeletar) {
        AlertDialog(
            onDismissRequest = {
                mostrarDialogDeletar = false
                tipoErroDeletar = 0
            },
            title = { Text(stringResource(R.string.delete_dialog_title), fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(stringResource(R.string.delete_dialog_body))
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = senhaConfirmacao,
                        onValueChange = { senhaConfirmacao = it },
                        label = { Text(stringResource(R.string.delete_dialog_password_label)) },
                        singleLine = true
                    )

                    // A UI decide qual string mostrar baseada no tipo de erro!
                    if (tipoErroDeletar == 1) {
                        Text(
                            text = stringResource(R.string.delete_error_wrong_password),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else if (tipoErroDeletar == 2) {
                        Text(
                            text = stringResource(R.string.delete_error_generic, detalheErroDeletar),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val user = auth.currentUser ?: return@Button
                        val credential = EmailAuthProvider.getCredential(
                            user.email ?: "", senhaConfirmacao
                        )
                        user.reauthenticate(credential)
                            .addOnSuccessListener {
                                // Remove dados do Firestore
                                db.collection("users").document(userUid).delete()
                                // Remove a conta do Firebase Auth
                                user.delete().addOnSuccessListener { onSair() }
                                    .addOnFailureListener { e ->
                                        tipoErroDeletar = 2
                                        detalheErroDeletar = e.message ?: "Erro desconhecido"
                                    }
                            }
                            .addOnFailureListener {
                                tipoErroDeletar = 1 // Senha incorreta
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.delete_dialog_confirm)) }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    mostrarDialogDeletar = false
                    tipoErroDeletar = 0
                }) {
                    Text(stringResource(R.string.delete_dialog_cancel))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Toggle de tema no topo
        val segmentedColors = SegmentedButtonDefaults.colors(
            activeContentColor = MaterialTheme.colorScheme.primary,
            inactiveContentColor = MaterialTheme.colorScheme.primary,
            activeContainerColor = Color.Transparent,
            inactiveContainerColor = Color.Transparent,
            activeBorderColor = MaterialTheme.colorScheme.primary,
            inactiveBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.align(Alignment.End)) {
            SegmentedButton(
                selected = !isDarkTheme,
                onClick = { onToggleTheme(false) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                colors = segmentedColors
            ) { Icon(Icons.Default.LightMode, stringResource(R.string.theme_light)) }
            SegmentedButton(
                selected = isDarkTheme,
                onClick = { onToggleTheme(true) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                colors = segmentedColors
            ) {
                Icon(
                    painterResource(R.drawable.ic_lua),
                    stringResource(R.string.theme_dark),
                    modifier = Modifier.scale(scaleX = -1f, scaleY = 1f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text(
            stringResource(R.string.profile_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // dados do usuario
        SectionTitle(stringResource(R.string.profile_section_personal))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text(stringResource(R.string.profile_name_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.profile_email_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        // endereco
        SectionTitle(stringResource(R.string.profile_section_address))

        OutlinedTextField(
            value = cep,
            onValueChange = { cep = it },
            label = { Text(stringResource(R.string.profile_cep_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            OutlinedTextField(
                value = rua,
                onValueChange = { rua = it },
                label = { Text(stringResource(R.string.profile_street_label)) },
                singleLine = true,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            OutlinedTextField(
                value = numero,
                onValueChange = { numero = it },
                label = { Text(stringResource(R.string.profile_number_label)) },
                singleLine = true,
                modifier = Modifier.width(80.dp)
            )
        }
        OutlinedTextField(
            value = bairro,
            onValueChange = { bairro = it },
            label = { Text(stringResource(R.string.profile_neighborhood_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            OutlinedTextField(
                value = cidade,
                onValueChange = { cidade = it },
                label = { Text(stringResource(R.string.profile_city_label)) },
                singleLine = true,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            OutlinedTextField(
                value = estado,
                onValueChange = { estado = it },
                label = { Text(stringResource(R.string.profile_state_label)) },
                singleLine = true,
                modifier = Modifier.width(72.dp)
            )
        }

        // salvar
        Button(
            onClick = {
                val dados = mapOf(
                    "nome" to nome,
                    "rua" to rua,
                    "numero" to numero,
                    "bairro" to bairro,
                    "cidade" to cidade,
                    "estado" to estado,
                    "cep" to cep
                )
                db.collection("users").document(userUid).update(dados)
                    .addOnSuccessListener { statusSalvar = 1 }
                    .addOnFailureListener { e ->
                        statusSalvar = 2
                        detalheErroSalvar = e.message ?: "Erro desconhecido"
                    }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.medium
        ) { Text(stringResource(R.string.profile_save_button)) }

        // A UI formata a mensagem de salvar
        if (statusSalvar == 1) {
            Text(
                text = stringResource(R.string.profile_save_success),
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 14.sp
            )
        } else if (statusSalvar == 2) {
            Text(
                text = stringResource(R.string.profile_save_error, detalheErroSalvar),
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(32.dp))

        // sair
        OutlinedButton(
            onClick = onSair,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.medium
        ) { Text(stringResource(R.string.profile_logout_button)) }

        Spacer(Modifier.height(12.dp))

        // deletar
        TextButton(
            onClick = { mostrarDialogDeletar = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.profile_delete_account), color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 4.dp)
    )
}
/*
package com.example.matchlist.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.matchlist.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(
    userUid: String,
    isDarkTheme: Boolean,
    onToggleTheme: (Boolean) -> Unit,
    onSair: () -> Unit
) {
    val db   = remember { FirebaseFirestore.getInstance() }
    val auth = remember { Firebase.auth }

    // campos editaveis dos dados do usuario
    var nome      by remember { mutableStateOf("") }
    var email     by remember { mutableStateOf(auth.currentUser?.email ?: "") }
    var rua       by remember { mutableStateOf("") }
    var numero    by remember { mutableStateOf("") }
    var bairro    by remember { mutableStateOf("") }
    var cidade    by remember { mutableStateOf("") }
    var estado    by remember { mutableStateOf("") }
    var cep       by remember { mutableStateOf("") }

    var mensagem  by remember { mutableStateOf("") }
    var carregando by remember { mutableStateOf(true) }

    // alertas
    var mostrarDialogDeletar by remember { mutableStateOf(false) }
    var senhaConfirmacao      by remember { mutableStateOf("") }
    var erroDeletar           by remember { mutableStateOf("") }

    // carrega Firestore ao abrir
    LaunchedEffect(userUid) {
        db.collection("users").document(userUid).get()
            .addOnSuccessListener { doc ->
                nome   = doc.getString("nome")   ?: ""
                rua    = doc.getString("rua")    ?: ""
                numero = doc.getString("numero") ?: ""
                bairro = doc.getString("bairro") ?: ""
                cidade = doc.getString("cidade") ?: ""
                estado = doc.getString("estado") ?: ""
                cep    = doc.getString("cep")    ?: ""
                carregando = false
            }
            .addOnFailureListener { carregando = false }
    }

    if (carregando) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    // alerta de confirmação de exclusão de conta
    if (mostrarDialogDeletar) {
        AlertDialog(
            onDismissRequest = { mostrarDialogDeletar = false; erroDeletar = "" },
            title = { Text("Deletar conta", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Esta ação é permanente e não pode ser desfeita. Digite sua senha para confirmar.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = senhaConfirmacao,
                        onValueChange = { senhaConfirmacao = it },
                        label = { Text("Sua senha atual") },
                        singleLine = true
                    )
                    if (erroDeletar.isNotEmpty()) {
                        Text(erroDeletar, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val user = auth.currentUser ?: return@Button
                        val credential = EmailAuthProvider.getCredential(
                            user.email ?: "", senhaConfirmacao
                        )
                        user.reauthenticate(credential)
                            .addOnSuccessListener {
                                // Remove dados do Firestore
                                db.collection("users").document(userUid).delete()
                                // Remove a conta do Firebase Auth
                                user.delete().addOnSuccessListener { onSair() }
                                    .addOnFailureListener { e -> erroDeletar = "Erro: ${e.message}" }
                            }
                            .addOnFailureListener { erroDeletar = "Senha incorreta." }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Deletar") }
            },
            dismissButton = {
                OutlinedButton(onClick = { mostrarDialogDeletar = false; erroDeletar = "" }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Toggle de tema no topo
        val segmentedColors = SegmentedButtonDefaults.colors(
            activeContentColor   = MaterialTheme.colorScheme.primary,
            inactiveContentColor = MaterialTheme.colorScheme.primary,
            activeContainerColor  = Color.Transparent,
            inactiveContainerColor = Color.Transparent,
            activeBorderColor    = MaterialTheme.colorScheme.primary,
            inactiveBorderColor  = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.align(Alignment.End)) {
            SegmentedButton(
                selected = !isDarkTheme,
                onClick  = { onToggleTheme(false) },
                shape    = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                colors   = segmentedColors
            ) { Icon(Icons.Default.LightMode, "Tema claro") }
            SegmentedButton(
                selected = isDarkTheme,
                onClick  = { onToggleTheme(true) },
                shape    = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                colors   = segmentedColors
            ) {
                Icon(
                    painterResource(R.drawable.ic_lua),
                    "Tema escuro",
                    modifier = Modifier.scale(scaleX = -1f, scaleY = 1f)
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Text("Meu Perfil", fontSize = 24.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp))

        // dados do usuario
        SectionTitle("Dados pessoais")

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome completo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )

        // endereco
        SectionTitle("Endereço")

        OutlinedTextField(
            value = cep,
            onValueChange = { cep = it },
            label = { Text("CEP") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            OutlinedTextField(
                value = rua,
                onValueChange = { rua = it },
                label = { Text("Rua") },
                singleLine = true,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            OutlinedTextField(
                value = numero,
                onValueChange = { numero = it },
                label = { Text("Nº") },
                singleLine = true,
                modifier = Modifier.width(80.dp)
            )
        }
        OutlinedTextField(
            value = bairro,
            onValueChange = { bairro = it },
            label = { Text("Bairro") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        )
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            OutlinedTextField(
                value = cidade,
                onValueChange = { cidade = it },
                label = { Text("Cidade") },
                singleLine = true,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )
            OutlinedTextField(
                value = estado,
                onValueChange = { estado = it },
                label = { Text("UF") },
                singleLine = true,
                modifier = Modifier.width(72.dp)
            )
        }

        // salvar
        Button(
            onClick = {
                val dados = mapOf(
                    "nome"   to nome,
                    "rua"    to rua,
                    "numero" to numero,
                    "bairro" to bairro,
                    "cidade" to cidade,
                    "estado" to estado,
                    "cep"    to cep
                )
                db.collection("users").document(userUid).update(dados)
                    .addOnSuccessListener { mensagem = "✅ Dados salvos com sucesso!" }
                    .addOnFailureListener { mensagem = "❌ Erro ao salvar: ${it.message}" }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.medium
        ) { Text("Salvar alterações") }

        if (mensagem.isNotEmpty()) {
            Text(mensagem, modifier = Modifier.padding(top = 12.dp), fontSize = 14.sp)
        }

        Spacer(Modifier.height(32.dp))

        // sair
        OutlinedButton(
            onClick = onSair,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.medium
        ) { Text("Sair da conta") }

        Spacer(Modifier.height(12.dp))

        // deletar
        TextButton(
            onClick = { mostrarDialogDeletar = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Deletar conta", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(48.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, top = 4.dp)
    )
}*/
