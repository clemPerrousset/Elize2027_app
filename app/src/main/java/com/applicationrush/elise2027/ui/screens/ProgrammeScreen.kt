package com.applicationrush.elise2027.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applicationrush.elise2027.data.model.CandidateInfo
import com.applicationrush.elise2027.data.model.PROGRAMME_THEMES
import com.applicationrush.elise2027.data.model.Programme
import com.applicationrush.elise2027.data.model.ProgrammeMeasure
import com.applicationrush.elise2027.data.model.ProgrammeTheme
import com.applicationrush.elise2027.data.repository.ProgrammeRepository
import com.applicationrush.elise2027.ui.theme.Background
import com.applicationrush.elise2027.ui.theme.OnSurface
import com.applicationrush.elise2027.ui.theme.OnSurfaceMuted
import com.applicationrush.elise2027.ui.theme.Surface
import com.applicationrush.elise2027.ui.theme.SurfaceVariant

private sealed interface ProgrammeState {
    data object Loading : ProgrammeState
    data class Loaded(val programme: Programme?) : ProgrammeState
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgrammeScreen(
    info: CandidateInfo,
    onBack: () -> Unit,
    onCompare: () -> Unit,
    showDisclaimer: Boolean,
    onDisclaimerAccepted: (dontShowAgain: Boolean) -> Unit,
) {
    val context = LocalContext.current
    val state by produceState<ProgrammeState>(ProgrammeState.Loading, info.id) {
        value = ProgrammeState.Loaded(ProgrammeRepository.get(context, info.id))
    }
    val partyColor = parseHexColor(info.colorHex)
    // Fermé pour cette ouverture ; "Ne plus afficher" le masque ensuite définitivement (DataStore).
    var disclaimerClosed by remember { mutableStateOf(false) }

    BackHandler(onBack = onBack)

    if (showDisclaimer && !disclaimerClosed) {
        ProgrammeDisclaimerDialog(
            onAccept = { dontShowAgain ->
                disclaimerClosed = true
                onDisclaimerAccepted(dontShowAgain)
            },
        )
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Programme", color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OnSurface)
                    }
                },
                actions = {
                    TextButton(onClick = onCompare) {
                        Text("⚖️ Comparer", color = OnSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background),
            )
        },
    ) { padding ->
        when (val s = state) {
            ProgrammeState.Loading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = OnSurfaceMuted, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
            }

            is ProgrammeState.Loaded -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { ProgrammeHeader(info, partyColor, s.programme) }

                val programme = s.programme
                if (programme == null) {
                    item {
                        InfoBox("Programme bientôt disponible pour ce candidat.")
                    }
                } else {
                    programme.programmeNote?.let { note -> item { InfoBox(note) } }

                    if (programme.highlights.isNotEmpty()) {
                        item { SectionTitle("En bref") }
                        item { Highlights(programme.highlights, partyColor) }
                    }

                    item { SectionTitle("Par thème") }
                    items(programme.themes, key = { it.id }) { theme ->
                        ThemeSection(theme, partyColor)
                    }

                    item { Footer(programme.updatedAt) }
                }
            }
        }
    }
}

@Composable
internal fun ProgrammeDisclaimerDialog(onAccept: (dontShowAgain: Boolean) -> Unit) {
    var dontShowAgain by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = { onAccept(false) },
        containerColor = Surface,
        icon = { Text("📢", fontSize = 28.sp) },
        title = {
            Text("Des programmes qui évoluent", color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    PROGRAMME_DISCLAIMER_TEXT,
                    color = OnSurfaceMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                )
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(role = Role.Checkbox) { dontShowAgain = !dontShowAgain },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = null,
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF7B7BFF), uncheckedColor = OnSurfaceMuted),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Ne plus afficher", color = OnSurface, fontSize = 14.sp)
                    Spacer(Modifier.width(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onAccept(dontShowAgain) }) {
                Text("J'ai compris", color = Color(0xFF7B7BFF), fontWeight = FontWeight.Bold)
            }
        },
    )
}

private const val PROGRAMME_DISCLAIMER_TEXT =
    "Les programmes peuvent évoluer au cours de la campagne. Certains candidats n'ont pas encore " +
        "publié de programme officiel : leur fiche repose alors sur leurs déclarations publiques et " +
        "sera complétée dès sa parution.\n\n" +
        "Mettez régulièrement l'application à jour pour disposer des dernières informations."

@Composable
private fun ProgrammeHeader(info: CandidateInfo, partyColor: Color, programme: Programme?) {
    val uriHandler = LocalUriHandler.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CandidateAvatar(info, partyColor, size = 72.dp)
            Spacer(Modifier.width(14.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(info.name, color = OnSurface, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                PartyChip(info.party, partyColor)
                programme?.let { StatusBadge(it.status) }
            }
        }

        programme?.statusNote?.let {
            Text(it, color = OnSurfaceMuted, fontSize = 12.sp, lineHeight = 17.sp)
        }

        val url = programme?.officialUrl
        if (url != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(partyColor.copy(alpha = 0.16f))
                    .border(1.dp, partyColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .clickable(role = Role.Button) { runCatching { uriHandler.openUri(url) } }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "📄 ${programme.officialUrlLabel ?: "Site officiel"} ↗",
                    color = OnSurface,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                )
            }
        }
    }
}

@Composable
internal fun StatusBadge(status: String) {
    val (label, color) = when (status) {
        "declared" -> "Candidature déclarée" to Color(0xFF4CAF50)
        "potential" -> "Candidature envisagée" to Color(0xFFFFB300)
        "withdrawn" -> "Candidature retirée" to OnSurfaceMuted
        else -> "Statut à confirmer" to OnSurfaceMuted
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(7.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(label, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
internal fun InfoBox(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceVariant)
            .padding(12.dp),
    ) {
        Text("ℹ️", fontSize = 13.sp)
        Spacer(Modifier.width(8.dp))
        Text(text, color = OnSurfaceMuted, fontSize = 12.sp, lineHeight = 17.sp)
    }
}

@Composable
internal fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        color = OnSurfaceMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun Highlights(highlights: List<String>, partyColor: Color) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        highlights.forEach { h ->
            Row {
                Text("★", color = partyColor, fontSize = 13.sp)
                Spacer(Modifier.width(8.dp))
                Text(h, color = OnSurface, fontSize = 14.sp, lineHeight = 19.sp)
            }
        }
    }
}

@Composable
private fun ThemeSection(theme: ProgrammeTheme, partyColor: Color) {
    val meta = PROGRAMME_THEMES[theme.id]
    val isEmpty = theme.measures.isEmpty()
    var expanded by rememberSaveable(theme.id) { mutableStateOf(false) }
    val chevronAngle by animateFloatAsState(if (expanded) 180f else 0f, label = "chevron_${theme.id}")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isEmpty, role = Role.Button) { expanded = !expanded }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(meta?.emoji ?: "•", fontSize = 20.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = meta?.label ?: theme.id,
                    color = if (isEmpty) OnSurfaceMuted else OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
                if (isEmpty) {
                    Text("Aucune position publiée à ce jour", color = OnSurfaceMuted, fontSize = 12.sp)
                }
            }
            if (!isEmpty) {
                Text(
                    text = "${theme.measures.size}",
                    color = partyColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(partyColor.copy(alpha = 0.16f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
                Spacer(Modifier.width(6.dp))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Replier" else "Déplier",
                    tint = OnSurfaceMuted,
                    modifier = Modifier.rotate(chevronAngle),
                )
            }
        }

        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column(
                modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                theme.summary?.let {
                    Text(it, color = OnSurfaceMuted, fontSize = 13.sp, fontStyle = FontStyle.Italic, lineHeight = 18.sp)
                }
                theme.measures.forEach { MeasureRow(it, partyColor) }
            }
        }
    }
}

@Composable
private fun MeasureRow(measure: ProgrammeMeasure, partyColor: Color) {
    val uriHandler = LocalUriHandler.current
    Row {
        Box(
            Modifier
                .padding(top = 7.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(partyColor),
        )
        Spacer(Modifier.width(10.dp))
        Column {
            Text(measure.text, color = OnSurface, fontSize = 14.sp, lineHeight = 19.sp)
            Spacer(Modifier.height(3.dp))
            val source = listOfNotNull(measure.sourceLabel, formatMonth(measure.date)).joinToString(" · ")
            Text(
                text = "Source : ${source.ifEmpty { "lien" }} ↗",
                color = OnSurfaceMuted,
                fontSize = 11.sp,
                modifier = Modifier.clickable(role = Role.Button) {
                    runCatching { uriHandler.openUri(measure.sourceUrl) }
                },
            )
        }
    }
}

@Composable
private fun Footer(updatedAt: String?) {
    Text(
        text = buildString {
            formatDay(updatedAt)?.let { append("Mis à jour le $it. ") }
            append("Synthèse réalisée à partir de sources publiques (programmes, sites officiels, interviews). ")
            append("Consultez les sources pour le détail des propositions.")
        },
        color = OnSurfaceMuted,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        modifier = Modifier.padding(top = 12.dp),
    )
}

private val MONTHS_FR = listOf(
    "janv.", "févr.", "mars", "avr.", "mai", "juin", "juil.", "août", "sept.", "oct.", "nov.", "déc.",
)

// "2026-05" → "mai 2026"
internal fun formatMonth(yearMonth: String?): String? {
    val parts = yearMonth?.split("-") ?: return null
    val month = parts.getOrNull(1)?.toIntOrNull() ?: return null
    return MONTHS_FR.getOrNull(month - 1)?.let { "$it ${parts[0]}" }
}

// "2026-09-23" → "23/09/2026"
internal fun formatDay(isoDate: String?): String? {
    val parts = isoDate?.split("-")?.takeIf { it.size == 3 } ?: return null
    return "${parts[2]}/${parts[1]}/${parts[0]}"
}
