package com.applicationrush.elise2027.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applicationrush.elise2027.data.model.CANDIDATES
import com.applicationrush.elise2027.data.model.VoteHistoryPoint
import com.applicationrush.elise2027.ui.theme.OnSurface
import com.applicationrush.elise2027.ui.theme.OnSurfaceMuted
import com.applicationrush.elise2027.ui.theme.Surface
import com.applicationrush.elise2027.ui.theme.SurfaceVariant
import com.applicationrush.elise2027.ui.viewmodel.VoteViewModel

// Nombre de séries affichées par défaut avant que l'utilisateur ne déplie tout
// (avec ~28 candidats un graphe multi-lignes complet devient vite illisible)
private const val DEFAULT_TOP_N = 8

private data class CandidateSeries(
    val id: String,
    val name: String,
    val color: Color,
    val points: List<VoteHistoryPoint>,
    val lastVotes: Int,
)

@Composable
fun ProgressionScreen(viewModel: VoteViewModel) {
    val history by viewModel.history.collectAsState()
    val isLoading by viewModel.isHistoryLoading.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadHistoryIfNeeded() }

    var showAll by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            "Progression des votes",
            color = OnSurface,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
        )
        Spacer(Modifier.height(16.dp))

        when {
            isLoading && history.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OnSurfaceMuted)
                }
            }
            history.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Pas encore de données", color = OnSurfaceMuted)
                }
            }
            else -> {
                val colorById = remember { CANDIDATES.associate { it.id to parseHexColor(it.colorHex) } }
                val nameById = remember { CANDIDATES.associate { it.id to it.name } }

                val allSeries = remember(history) {
                    history
                        .groupBy { it.candidate_id }
                        .mapNotNull { (id, points) ->
                            val name = nameById[id] ?: return@mapNotNull null
                            val sorted = points.sortedBy { it.date }
                            CandidateSeries(
                                id = id,
                                name = name,
                                color = colorById[id] ?: Color.Gray,
                                points = sorted,
                                lastVotes = sorted.lastOrNull()?.votes ?: 0,
                            )
                        }
                        .sortedByDescending { it.lastVotes }
                }

                val visibleSeries = if (showAll) allSeries else allSeries.take(DEFAULT_TOP_N)

                LineChart(
                    series = visibleSeries,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Surface)
                        .padding(12.dp),
                )

                Spacer(Modifier.height(12.dp))

                if (allSeries.size > DEFAULT_TOP_N) {
                    Text(
                        text = if (showAll) "Afficher le top $DEFAULT_TOP_N" else "Afficher les ${allSeries.size} candidats",
                        color = OnSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { showAll = !showAll }
                            .padding(vertical = 8.dp),
                    )
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(visibleSeries, key = { it.id }) { s -> LegendRow(s) }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun LegendRow(series: CandidateSeries) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(series.color),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = series.name,
            color = OnSurface,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${series.lastVotes}",
            color = OnSurfaceMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun LineChart(series: List<CandidateSeries>, modifier: Modifier = Modifier) {
    val allPoints = series.flatMap { it.points }
    val minDate = allPoints.minOfOrNull { it.date } ?: 0L
    val maxDate = allPoints.maxOfOrNull { it.date } ?: 0L
    val maxVotes = (allPoints.maxOfOrNull { it.votes } ?: 0).coerceAtLeast(1)
    val dateSpan = (maxDate - minDate).coerceAtLeast(1L)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Grille horizontale légère
        val gridColor = SurfaceVariant
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = h * i / gridLines
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f,
            )
        }

        series.forEach { s ->
            if (s.points.isEmpty()) return@forEach
            val path = androidx.compose.ui.graphics.Path()
            s.points.forEachIndexed { index, point ->
                val x = w * (point.date - minDate).toFloat() / dateSpan.toFloat()
                val y = h - (h * point.votes.toFloat() / maxVotes.toFloat())
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            drawPath(
                path = path,
                color = s.color,
                style = Stroke(width = 4f, cap = StrokeCap.Round),
            )
        }
    }
}
