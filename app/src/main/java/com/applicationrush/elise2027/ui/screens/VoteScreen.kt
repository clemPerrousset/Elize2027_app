package com.applicationrush.elise2027.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.applicationrush.elise2027.R
import com.applicationrush.elise2027.data.model.CandidateInfo
import com.applicationrush.elise2027.data.model.CandidateUiState
import com.applicationrush.elise2027.ui.theme.Background
import com.applicationrush.elise2027.ui.theme.OnSurface
import com.applicationrush.elise2027.ui.theme.OnSurfaceMuted
import com.applicationrush.elise2027.ui.theme.Surface
import com.applicationrush.elise2027.ui.theme.SurfaceVariant
import com.applicationrush.elise2027.ui.viewmodel.VoteViewModel
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoteScreen(viewModel: VoteViewModel) {
    val candidates by viewModel.candidates.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isMockMode by viewModel.isMockMode.collectAsState()
    val error by viewModel.error.collectAsState()
    val cooldown by viewModel.cooldownSeconds.collectAsState()

    val snackbar = remember { SnackbarHostState() }
    var explosionTrigger by remember { mutableIntStateOf(0) }
    var explosionOffset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(error) {
        if (error != null) {
            snackbar.showSnackbar("Erreur : $error")
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Background,
        snackbarHost = {
            SnackbarHost(snackbar) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFF2A2A45),
                    contentColor = OnSurface,
                )
            }
        },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Élyze 2027",
                        color = OnSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background),
                actions = {
                    // Temporary mock toggle button
                    TextButton(onClick = { viewModel.toggleMockMode() }) {
                        Text(
                            if (isMockMode) "🎭 Live" else "🎭 Mock",
                            color = if (isMockMode) Color(0xFFFFD700) else OnSurfaceMuted,
                            fontSize = 13.sp,
                        )
                    }
                    // Refresh button
                    IconButton(
                        onClick = { viewModel.refresh() },
                        enabled = !isLoading && !isMockMode,
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = OnSurfaceMuted,
                            )
                        } else {
                            Text("↻", color = if (isMockMode) OnSurfaceMuted else OnSurface, fontSize = 22.sp)
                        }
                    }
                },
            )
        },
    ) { padding ->
        if (candidates.isEmpty() && !isLoading) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text("Aucun candidat", color = OnSurfaceMuted)
            }
            return@Scaffold
        }

        Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { Spacer(Modifier.height(4.dp)) }
            itemsIndexed(
                items = candidates,
                key = { _, item -> item.info.id },
            ) { index, candidate ->
                CandidateCard(
                    candidate = candidate,
                    rank = index + 1,
                    enabled = cooldown == 0,
                    onVote = { offset ->
                        explosionOffset = offset
                        explosionTrigger++
                        viewModel.vote(candidate.info.id)
                    },
                    modifier = Modifier.animateItem(
                        fadeInSpec = tween(300),
                        fadeOutSpec = tween(300),
                        placementSpec = spring(stiffness = 300f, dampingRatio = 0.7f),
                    ),
                )
            }
            item { Spacer(Modifier.height(80.dp)) }
        }

        // Cooldown overlay — flottant en bas par-dessus la liste
        AnimatedVisibility(
            visible = cooldown > 0,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xCC1E1E35))
                    .padding(horizontal = 28.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Attendez $cooldown…",
                    color = OnSurface,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        ConfettiExplosion(
            trigger = explosionTrigger,
            origin = explosionOffset,
            modifier = Modifier.fillMaxSize(),
        )
        } // Box
    }
}

@Composable
fun CandidateCard(
    candidate: CandidateUiState,
    rank: Int,
    enabled: Boolean = true,
    onVote: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val partyColor = parseHexColor(candidate.info.colorHex)
    val secondaryColor = candidate.info.secondaryColorHex?.let { parseHexColor(it) }
    val brush = secondaryColor?.let { Brush.horizontalGradient(listOf(partyColor, it)) }
    val progress by animateFloatAsState(
        targetValue = candidate.progressFraction,
        animationSpec = tween(600),
        label = "progress_${candidate.info.id}",
    )

    var cardCenter by remember { mutableStateOf(Offset.Zero) }

    val borderMod = if (candidate.isVotedFor) {
        if (brush != null)
            Modifier.border(2.dp, brush, RoundedCornerShape(16.dp))
        else
            Modifier.border(2.dp, partyColor, RoundedCornerShape(16.dp))
    } else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .then(borderMod)
            .background(if (candidate.isVotedFor) partyColor.copy(alpha = 0.08f) else Surface)
            .alpha(if (enabled) 1f else 0.5f)
            .onGloballyPositioned { coords ->
                val b = coords.boundsInWindow()
                cardCenter = Offset(b.center.x, b.center.y)
            }
            .clickable(enabled = enabled) { onVote(cardCenter) }
    ) {
        Column {
            Row(
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Rank
                Text(
                    text = "$rank",
                    color = OnSurfaceMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(20.dp),
                )

                Spacer(Modifier.width(8.dp))

                // Avatar
                CandidateAvatar(candidate.info, partyColor)

                Spacer(Modifier.width(12.dp))

                // Name + party
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = candidate.info.name,
                        color = OnSurface,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    PartyChip(candidate.info.party, partyColor)
                }

                Spacer(Modifier.width(8.dp))

                // Vote count
                Column(horizontalAlignment = Alignment.End) {
                    AnimatedVisibility(
                        visible = candidate.isVotedFor,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut(),
                    ) {
                        Text("✓", color = partyColor, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    if (candidate.voteCount > 0) {
                        Text(
                            text = formatCount(candidate.voteCount),
                            color = OnSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                        Text(
                            text = if (candidate.voteCount == 1) "vote" else "votes",
                            color = OnSurfaceMuted,
                            fontSize = 11.sp,
                        )
                    }
                }
            }

            // Progress bar
            if (candidate.voteCount > 0) {
                if (brush != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(SurfaceVariant),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(brush),
                        )
                    }
                } else {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = partyColor,
                        trackColor = SurfaceVariant,
                        strokeCap = StrokeCap.Round,
                    )
                }
            }

            // Party color stripe at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(brush ?: Brush.horizontalGradient(listOf(partyColor.copy(alpha = 0.5f), partyColor.copy(alpha = 0.5f)))),
            )
        }
    }
}

@Composable
private fun CandidateAvatar(info: CandidateInfo, partyColor: Color) {
    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(partyColor.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center,
    ) {
        if (info.photoUrl != null) {
            val context = LocalContext.current
            var loadFailed by remember { mutableStateOf(false) }
            if (!loadFailed) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(info.photoUrl)
                        .addHeader("User-Agent", "Elyze2027App/1.0 (Android; contact@elyze2027.fr)")
                        .crossfade(true)
                        .build(),
                    contentDescription = info.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    onError = { loadFailed = true },
                )
            } else {
                InitialsText(info.name, partyColor)
            }
        } else {
            InitialsText(info.name, partyColor)
        }
    }
}

@Composable
private fun InitialsText(name: String, partyColor: Color) {
    val initials = name
        .split(" ")
        .filter { it.isNotEmpty() && it[0].isLetter() }
        .take(2)
        .joinToString("") { it[0].toString() }
        .uppercase()
    Text(
        text = initials,
        color = partyColor,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
    )
}

@Composable
private fun PartyChip(party: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = party,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun parseHexColor(hex: String): Color {
    return runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(Color.Gray)
}

private fun formatCount(count: Int): String {
    return if (count >= 1000) {
        val thousands = count / 1000
        val remainder = (count % 1000) / 100
        if (remainder == 0) "${thousands}k" else "${thousands},${remainder}k"
    } else {
        count.toString()
    }
}

@Composable
private fun ConfettiExplosion(
    trigger: Int,
    origin: Offset,
    modifier: Modifier = Modifier,
) {
    if (trigger == 0) return

    data class Particle(val velX: Float, val velY: Float, val emoji: String)

    val emojis = listOf("🇫🇷", "🇫🇷", "🇫🇷", "⭐", "🗼")
    val particles = remember(trigger) {
        List(16) { i ->
            val angle = (i * (360f / 16f) + Random.nextFloat() * 12f) * PI.toFloat() / 180f
            val speed = 160f + Random.nextFloat() * 220f
            Particle(
                velX = cos(angle) * speed,
                velY = sin(angle) * speed,
                emoji = emojis[i % emojis.size],
            )
        }
    }

    var progress by remember(trigger) { mutableFloatStateOf(0f) }

    LaunchedEffect(trigger) {
        val startNanos = withFrameNanos { it }
        val durationNanos = 850_000_000L
        while (progress < 1f) {
            withFrameNanos { frameNanos ->
                progress = ((frameNanos - startNanos).toFloat() / durationNanos).coerceAtMost(1f)
            }
        }
    }

    Box(modifier = modifier) {
        particles.forEach { p ->
            val t = progress
            val x = origin.x + p.velX * t
            val y = origin.y + p.velY * t + 480f * t * t  // gravité
            val alpha = (1f - t * 1.4f).coerceIn(0f, 1f)

            Text(
                text = p.emoji,
                fontSize = 18.sp,
                modifier = Modifier
                    .offset { IntOffset((x - 12f).roundToInt(), (y - 12f).roundToInt()) }
                    .graphicsLayer { this.alpha = alpha },
            )
        }
    }
}
