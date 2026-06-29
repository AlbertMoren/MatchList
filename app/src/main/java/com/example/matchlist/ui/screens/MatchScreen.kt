package com.example.matchlist.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.matchlist.R
import com.example.matchlist.ui.theme.MdAccentDislike
import com.example.matchlist.ui.theme.MdAccentLike
import com.example.matchlist.viewmodel.MatchStatus
import com.example.matchlist.viewmodel.MatchViewModel
import kotlinx.coroutines.launch

data class Produto(
    val id: String,
    val nome: String,
    val preco: String,
    val imagemUrl: String? = null
)

@Composable
fun MatchScreen(viewModel: MatchViewModel) {
    val produtoAtual = viewModel.produtos.getOrNull(viewModel.indiceAtual)

    val offsetX = remember { Animatable(0f) }
    val scope   = rememberCoroutineScope()

    LaunchedEffect(viewModel.indiceAtual) { offsetX.snapTo(0f) }

    val density        = LocalDensity.current
    val screenWidthPx  = with(density) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val swipeThreshold = screenWidthPx * 0.30f

    val statusText = when (val s = viewModel.status) {
        is MatchStatus.Loading -> stringResource(R.string.match_status_loading)
        is MatchStatus.Empty   -> stringResource(R.string.match_status_empty)
        is MatchStatus.Ready   -> ""
        is MatchStatus.Error   -> stringResource(R.string.match_status_error, s.message)
    }
    val finishedText = stringResource(R.string.match_status_done)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        if (statusText.isNotEmpty()) {
            Text(
                text = statusText,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (produtoAtual != null) {
                SwipeableProductCard(
                    produto        = produtoAtual,
                    offsetX        = offsetX,
                    screenWidthPx  = screenWidthPx,
                    swipeThreshold = swipeThreshold,
                    onLike = {
                        scope.launch {
                            offsetX.animateTo(screenWidthPx, tween(300))
                            viewModel.curtir()
                        }
                    },
                    onDislike = {
                        scope.launch {
                            offsetX.animateTo(-screenWidthPx, tween(300))
                            viewModel.pular()
                        }
                    }
                )
            } else if (viewModel.produtos.isNotEmpty()) {
                Text(finishedText, textAlign = TextAlign.Center)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            CircleActionButton(
                color = MdAccentDislike,
                icon  = Icons.Default.Close,
                desc  = stringResource(R.string.match_action_dislike)
            ) {
                scope.launch {
                    offsetX.animateTo(-screenWidthPx, tween(300))
                    viewModel.pular()
                }
            }
            Spacer(Modifier.width(32.dp))
            CircleActionButton(
                color = MdAccentLike,
                icon  = Icons.Default.Favorite,
                desc  = stringResource(R.string.match_action_like)
            ) {
                scope.launch {
                    offsetX.animateTo(screenWidthPx, tween(300))
                    viewModel.curtir()
                }
            }
        }
    }
}

@Composable
private fun SwipeableProductCard(
    produto: Produto,
    offsetX: Animatable<Float, *>,
    screenWidthPx: Float,
    swipeThreshold: Float,
    onLike: () -> Unit,
    onDislike: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val swipeProgress = (offsetX.value / swipeThreshold).coerceIn(-1f, 1f)
    val showLike    = swipeProgress > 0.2f
    val showDislike = swipeProgress < -0.2f

    val likeLabel  = stringResource(R.string.match_label_like)
    val nopeLabel  = stringResource(R.string.match_label_nope)
    val noImageStr = stringResource(R.string.match_no_image)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ    = (offsetX.value / screenWidthPx) * 12f
            }
            .pointerInput(produto.id) {
                detectDragGestures(
                    onDrag = { change, drag ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + drag.x) }
                    },
                    onDragEnd = {
                        scope.launch {
                            when {
                                offsetX.value > swipeThreshold  -> onLike()
                                offsetX.value < -swipeThreshold -> onDislike()
                                else -> offsetX.animateTo(0f, tween(300))
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch { offsetX.animateTo(0f, tween(300)) }
                    }
                )
            }
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                SubcomposeAsyncImage(
                    model = produto.imagemUrl,
                    contentDescription = produto.nome,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(noImageStr, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = produto.nome,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color.Black,
                                offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                blurRadius = 4f
                            )
                        )
                    )
                    Text(
                        text = produto.preco,
                        color = Color(0xFFE0E0E0),
                        fontSize = 18.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (showLike) {
                    SwipeLabel(
                        text  = likeLabel,
                        color = MdAccentLike,
                        modifier = Modifier.align(Alignment.TopStart).padding(24.dp),
                        alpha = swipeProgress
                    )
                }
                if (showDislike) {
                    SwipeLabel(
                        text  = nopeLabel,
                        color = MdAccentDislike,
                        modifier = Modifier.align(Alignment.TopEnd).padding(24.dp),
                        alpha = -swipeProgress
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeLabel(text: String, color: Color, modifier: Modifier, alpha: Float) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = (alpha * 0.85f).coerceIn(0f, 0.85f))
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun CircleActionButton(
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.size(64.dp).clip(CircleShape),
        color = color.copy(alpha = 0.12f),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, desc, tint = color, modifier = Modifier.size(32.dp))
        }
    }
}