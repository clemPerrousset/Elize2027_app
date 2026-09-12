package com.applicationrush.elise2027.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applicationrush.elise2027.data.model.CANDIDATES
import com.applicationrush.elise2027.data.model.CandidateInfo
import com.applicationrush.elise2027.data.model.VoteHistoryPoint
import com.applicationrush.elise2027.ui.theme.OnSurface
import com.applicationrush.elise2027.ui.theme.OnSurfaceMuted
import com.applicationrush.elise2027.ui.theme.Surface
import com.applicationrush.elise2027.ui.theme.SurfaceVariant
import com.applicationrush.elise2027.ui.viewmodel.VoteViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

// Nombre de séries affichées par défaut avant que l'utilisateur ne déplie tout
// (avec ~28 candidats un graphe multi-lignes complet devient vite illisible)
private const val DEFAULT_TOP_N = 8

private val DateAxisFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.FRENCH)

// Dimensions de la zone de graphe (cf. ProgressionChart) — partagées entre le Canvas et
// le calque d'avatars superposé, pour que les deux restent parfaitement synchronisés.
private val ChartCardPadding = 12.dp
private val ChartAvatarSize = 26.dp
private val ChartLeftAxisWidth = 34.dp
private val ChartBottomAxisHeight = 20.dp
private val ChartTopGap = 4.dp
private val ChartLabelGap = 4.dp

/** Un point d'historique agrégé : le dernier relevé d'un jour civil donné. */
private data class DayPoint(val day: LocalDate, val votes: Int)

private data class CandidateSeries(
    val info: CandidateInfo,
    val color: Color,
    val dayPoints: List<DayPoint>,
    val lastVotes: Int,
) {
    val id: String get() = info.id
    val name: String get() = info.name
}

/**
 * Regroupe les points d'historique bruts (plusieurs snapshots/jour côté backend) en un seul
 * point par jour civil (fuseau horaire local de l'appareil), en conservant pour chaque jour
 * le relevé le plus récent (date la plus élevée). Résultat trié par jour croissant.
 */
private fun bucketByDay(points: List<VoteHistoryPoint>): List<DayPoint> {
    return points
        .sortedBy { it.date }
        .groupBy { Instant.ofEpochSecond(it.date).atZone(ZoneId.systemDefault()).toLocalDate() }
        .map { (day, pointsForDay) -> DayPoint(day, pointsForDay.last().votes) }
        .sortedBy { it.day }
}

/** Arrondit vers le haut à un nombre "rond" (1/2/5 × 10^n) pour des graduations lisibles. */
private fun niceCeilingMax(value: Int): Int {
    if (value <= 0) return 1
    val magnitude = Math.pow(10.0, Math.floor(Math.log10(value.toDouble()))).toInt().coerceAtLeast(1)
    val residual = value.toDouble() / magnitude
    val niceResidual = when {
        residual <= 1.0 -> 1.0
        residual <= 2.0 -> 2.0
        residual <= 5.0 -> 5.0
        else -> 10.0
    }
    return (niceResidual * magnitude).toInt().coerceAtLeast(1)
}

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
                val infoById = remember { CANDIDATES.associateBy { it.id } }
                val colorById = remember { CANDIDATES.associate { it.id to parseHexColor(it.colorHex) } }

                val allSeries = remember(history) {
                    history
                        .groupBy { it.candidate_id }
                        .mapNotNull { (id, points) ->
                            val info = infoById[id] ?: return@mapNotNull null
                            val dayPoints = bucketByDay(points)
                            if (dayPoints.isEmpty()) return@mapNotNull null
                            CandidateSeries(
                                info = info,
                                color = colorById[id] ?: Color.Gray,
                                dayPoints = dayPoints,
                                lastVotes = dayPoints.last().votes,
                            )
                        }
                        .sortedByDescending { it.lastVotes }
                }

                val visibleSeries = if (showAll) allSeries else allSeries.take(DEFAULT_TOP_N)

                ProgressionChart(
                    series = visibleSeries,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.2f),
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

/**
 * Graphe de progression : une courbe par candidat (jour → nombre de votes), avec grille et
 * labels d'axes, et l'avatar du candidat superposé à l'extrémité (jour le plus récent) de
 * chaque courbe visible.
 *
 * Le Canvas et le calque d'avatars sont deux enfants séparés d'un même [BoxWithConstraints] :
 * le Canvas (avec son fond arrondi) est clippé pour un rendu propre, tandis que le calque
 * d'avatars ne l'est pas, pour que les avatars ne soient jamais rognés par les coins arrondis
 * même quand ils débordent légèrement du cadre (cas attendu pour le dernier point de la courbe).
 * Les deux utilisent exactement la même formule de mise à l'échelle date/votes → pixel pour
 * rester parfaitement synchronisés.
 */
@Composable
private fun ProgressionChart(series: List<CandidateSeries>, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val allDayPoints = series.flatMap { it.dayPoints }
    val minEpochDay = allDayPoints.minOfOrNull { it.day.toEpochDay() } ?: 0L
    val maxEpochDay = allDayPoints.maxOfOrNull { it.day.toEpochDay() } ?: 0L
    val daySpan = (maxEpochDay - minEpochDay).coerceAtLeast(1L)
    val maxVotes = niceCeilingMax((allDayPoints.maxOfOrNull { it.votes } ?: 0).coerceAtLeast(1))

    BoxWithConstraints(modifier = modifier) {
        val cardPaddingPx = with(density) { ChartCardPadding.toPx() }
        val avatarSizePx = with(density) { ChartAvatarSize.toPx() }
        val avatarRadiusPx = avatarSizePx / 2f
        val leftAxisPx = with(density) { ChartLeftAxisWidth.toPx() }
        val bottomAxisPx = with(density) { ChartBottomAxisHeight.toPx() }
        val topGapPx = with(density) { ChartTopGap.toPx() }
        val labelGapPx = with(density) { ChartLabelGap.toPx() }

        // Taille du contenu du Canvas, une fois la marge intérieure de la carte retirée.
        val contentWidthPx = with(density) { maxWidth.toPx() } - 2 * cardPaddingPx
        val contentHeightPx = with(density) { maxHeight.toPx() } - 2 * cardPaddingPx

        // Zone de tracé effective à l'intérieur du Canvas : on réserve de la place à gauche pour
        // les labels de l'axe Y, en bas pour les labels de l'axe X, et en haut pour que l'avatar
        // du point le plus haut ne soit pas rogné par le bord supérieur.
        val plotLeft = leftAxisPx
        val plotRight = contentWidthPx
        val plotTop = avatarRadiusPx + topGapPx
        val plotBottom = (contentHeightPx - bottomAxisPx).coerceAtLeast(plotTop + 1f)

        fun xForDay(day: LocalDate): Float {
            val frac = (day.toEpochDay() - minEpochDay).toFloat() / daySpan.toFloat()
            return plotLeft + (plotRight - plotLeft) * frac
        }

        fun yForVotes(votes: Int): Float {
            val frac = votes.toFloat() / maxVotes.toFloat()
            return plotBottom - (plotBottom - plotTop) * frac
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Surface),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(ChartCardPadding),
            ) {
                val labelStyle = TextStyle(color = OnSurfaceMuted, fontSize = 10.sp)

                // Grille horizontale + labels de l'axe Y (nombre de votes).
                val yGridLines = 4
                for (i in 0..yGridLines) {
                    val votesAtLine = maxVotes * (yGridLines - i) / yGridLines
                    val y = plotTop + (plotBottom - plotTop) * i / yGridLines
                    drawLine(
                        color = SurfaceVariant,
                        start = Offset(plotLeft, y),
                        end = Offset(plotRight, y),
                        strokeWidth = 1f,
                    )
                    val measured = textMeasurer.measure(votesAtLine.toString(), style = labelStyle)
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(
                            (plotLeft - measured.size.width - labelGapPx).coerceAtLeast(0f),
                            (y - measured.size.height / 2f)
                                .coerceIn(0f, (size.height - measured.size.height).coerceAtLeast(0f)),
                        ),
                    )
                }

                // Labels de l'axe X (dates) : premier jour, dernier jour, et 1-2 jours
                // intermédiaires selon l'étendue des données.
                val minDay = LocalDate.ofEpochDay(minEpochDay)
                val maxDay = LocalDate.ofEpochDay(maxEpochDay)
                val totalDays = daySpan + 1
                val labelDays: List<LocalDate> = when {
                    totalDays <= 3 -> listOf(minDay, maxDay)
                    totalDays <= 6 -> listOf(minDay, LocalDate.ofEpochDay(minEpochDay + daySpan / 2), maxDay)
                    else -> listOf(
                        minDay,
                        LocalDate.ofEpochDay(minEpochDay + daySpan / 3),
                        LocalDate.ofEpochDay(minEpochDay + daySpan * 2 / 3),
                        maxDay,
                    )
                }.distinct()

                labelDays.forEach { day ->
                    val measured = textMeasurer.measure(DateAxisFormatter.format(day), style = labelStyle)
                    val x = xForDay(day)
                    drawText(
                        textLayoutResult = measured,
                        topLeft = Offset(
                            (x - measured.size.width / 2f)
                                .coerceIn(0f, (size.width - measured.size.width).coerceAtLeast(0f)),
                            plotBottom + labelGapPx,
                        ),
                    )
                }

                // Courbes : une ligne par candidat, ou un point plein si une seule valeur.
                series.forEach { s ->
                    if (s.dayPoints.isEmpty()) return@forEach
                    if (s.dayPoints.size == 1) {
                        val p = s.dayPoints[0]
                        drawCircle(
                            color = s.color,
                            radius = 6f,
                            center = Offset(xForDay(p.day), yForVotes(p.votes)),
                        )
                        return@forEach
                    }
                    val path = androidx.compose.ui.graphics.Path()
                    s.dayPoints.forEachIndexed { index, point ->
                        val x = xForDay(point.day)
                        val y = yForVotes(point.votes)
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

        // Avatars superposés à l'extrémité (jour le plus récent) de chaque courbe visible.
        // Volontairement en dehors du Box clippé ci-dessus pour ne jamais être rognés par les
        // coins arrondis, y compris quand ils débordent légèrement du cadre à droite.
        series.forEach { s ->
            val last = s.dayPoints.lastOrNull() ?: return@forEach
            val x = cardPaddingPx + xForDay(last.day)
            val y = cardPaddingPx + yForVotes(last.votes)
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            (x - avatarRadiusPx).roundToInt(),
                            (y - avatarRadiusPx).roundToInt(),
                        )
                    }
                    .size(ChartAvatarSize)
                    .border(1.5.dp, s.color, CircleShape),
            ) {
                CandidateAvatar(info = s.info, partyColor = s.color, size = ChartAvatarSize)
            }
        }
    }
}
