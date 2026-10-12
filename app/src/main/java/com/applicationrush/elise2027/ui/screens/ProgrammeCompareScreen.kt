package com.applicationrush.elise2027.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applicationrush.elise2027.data.model.CANDIDATES
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

private enum class CompareSide { LEFT, RIGHT }

private sealed interface CompareState {
    data object Loading : CompareState
    data class Loaded(val left: Programme?, val right: Programme?) : CompareState
}

// Deux programmes côte à côte, thème par thème : chaque thème est une seule carte à deux colonnes,
// pour lire les deux positions d'un coup d'œil. Le second candidat se choisit dans une feuille
// de sélection, ouverte d'emblée à l'arrivée sur l'écran.
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProgrammeCompareScreen(
    initialLeftId: String,
    onBack: () -> Unit,
    showDisclaimer: Boolean,
    onDisclaimerAccepted: (dontShowAgain: Boolean) -> Unit,
) {
    val context = LocalContext.current
    var leftId by rememberSaveable { mutableStateOf(initialLeftId) }
    var rightId by rememberSaveable { mutableStateOf<String?>(null) }
    var pickerSide by rememberSaveable { mutableStateOf<CompareSide?>(CompareSide.RIGHT) }
    var disclaimerClosed by remember { mutableStateOf(false) }

    val left = CANDIDATES.firstOrNull { it.id == leftId }
    val right = rightId?.let { id -> CANDIDATES.firstOrNull { it.id == id } }
    val leftColor = left?.let { parseHexColor(it.colorHex) } ?: OnSurfaceMuted
    val rightColor = right?.let { parseHexColor(it.colorHex) } ?: OnSurfaceMuted

    val state by produceState<CompareState>(CompareState.Loading, leftId, rightId) {
        value = CompareState.Loaded(
            left = ProgrammeRepository.get(context, leftId),
            right = rightId?.let { ProgrammeRepository.get(context, it) },
        )
    }
    val loaded = state as? CompareState.Loaded

    BackHandler(onBack = onBack)

    // L'avertissement attend la fermeture de la feuille de sélection, pour ne pas s'empiler dessus.
    if (showDisclaimer && !disclaimerClosed && pickerSide == null) {
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
                title = { Text("Comparer", color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item(key = "headers") {
                Row(verticalAlignment = Alignment.Top) {
                    CompareCandidateHeader(
                        info = left,
                        color = leftColor,
                        programme = loaded?.left,
                        onClick = { pickerSide = CompareSide.LEFT },
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(
                        onClick = {
                            val r = rightId ?: return@IconButton
                            rightId = leftId
                            leftId = r
                        },
                        enabled = rightId != null,
                        modifier = Modifier.padding(top = 18.dp),
                    ) {
                        Text("⇄", color = if (rightId != null) OnSurface else OnSurfaceMuted, fontSize = 20.sp)
                    }
                    CompareCandidateHeader(
                        info = right,
                        color = rightColor,
                        programme = loaded?.right,
                        onClick = { pickerSide = CompareSide.RIGHT },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Rappel compact des deux colonnes, collé en haut pendant le défilement.
            stickyHeader(key = "names") {
                CompareNamesStrip(left, leftColor, right, rightColor)
            }

            when (val s = state) {
                CompareState.Loading -> item(key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OnSurfaceMuted, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                    }
                }

                is CompareState.Loaded -> {
                    val leftHighlights = s.left?.highlights.orEmpty()
                    val rightHighlights = s.right?.highlights.orEmpty()
                    if (leftHighlights.isNotEmpty() || rightHighlights.isNotEmpty()) {
                        item(key = "brief_title") { SectionTitle("En bref") }
                        item(key = "brief") {
                            CompareCard {
                                SideBySide(
                                    left = { HighlightsColumn(leftHighlights, leftColor) },
                                    right = {
                                        if (right != null) HighlightsColumn(rightHighlights, rightColor)
                                        else EmptyText("Choisissez un candidat à comparer")
                                    },
                                )
                            }
                        }
                    }

                    item(key = "themes_title") { SectionTitle("Par thème") }
                    items(PROGRAMME_THEMES.keys.toList(), key = { it }) { themeId ->
                        CompareThemeCard(
                            themeId = themeId,
                            left = s.left,
                            right = s.right,
                            leftColor = leftColor,
                            rightColor = rightColor,
                            hasRight = right != null,
                        )
                    }

                    item(key = "footer") { CompareFooter(left, s.left, right, s.right) }
                }
            }
        }
    }

    pickerSide?.let { side ->
        CandidatePickerSheet(
            title = if (side == CompareSide.LEFT) "Choisir un candidat" else "Comparer avec…",
            selectedId = if (side == CompareSide.LEFT) leftId else rightId,
            excludedId = if (side == CompareSide.LEFT) rightId else leftId,
            onPick = { id ->
                if (side == CompareSide.LEFT) leftId = id else rightId = id
                pickerSide = null
            },
            onDismiss = { pickerSide = null },
        )
    }
}

@Composable
private fun CompareCandidateHeader(
    info: CandidateInfo?,
    color: Color,
    programme: Programme?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (info == null) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, OnSurfaceMuted.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text("+", color = OnSurfaceMuted, fontSize = 26.sp)
            }
            Text("Choisir un candidat", color = OnSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
            return@Column
        }

        CandidateAvatar(info, color, size = 60.dp)
        Text(
            info.name,
            color = OnSurface,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        PartyChip(info.party, color)
        programme?.let {
            StatusBadge(it.status)
            Text(
                if (it.officialUrlIsProgramme) "Programme officiel publié" else "Pas de programme officiel publié",
                color = OnSurfaceMuted,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
        Text("Changer ▾", color = OnSurfaceMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CompareNamesStrip(left: CandidateInfo?, leftColor: Color, right: CandidateInfo?, rightColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Background)
            .padding(vertical = 6.dp),
    ) {
        NameTag(left?.name ?: "—", leftColor, Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        NameTag(right?.name ?: "À choisir", rightColor, Modifier.weight(1f))
    }
}

@Composable
private fun NameTag(name: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(name, color = OnSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun CompareCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface),
    ) { content() }
}

// Deux colonnes de même largeur séparées par un filet ; la hauteur suit la plus longue.
@Composable
private fun SideBySide(left: @Composable () -> Unit, right: @Composable () -> Unit) {
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(12.dp)) {
        Box(Modifier.weight(1f)) { left() }
        Box(
            Modifier
                .padding(horizontal = 10.dp)
                .width(1.dp)
                .fillMaxHeight()
                .background(SurfaceVariant),
        )
        Box(Modifier.weight(1f)) { right() }
    }
}

@Composable
private fun HighlightsColumn(highlights: List<String>, color: Color) {
    if (highlights.isEmpty()) {
        EmptyText("Pas de mesure phare")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        highlights.forEach { h ->
            Row {
                Text("★", color = color, fontSize = 12.sp)
                Spacer(Modifier.width(6.dp))
                Text(h, color = OnSurface, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}

@Composable
private fun CompareThemeCard(
    themeId: String,
    left: Programme?,
    right: Programme?,
    leftColor: Color,
    rightColor: Color,
    hasRight: Boolean,
) {
    val meta = PROGRAMME_THEMES[themeId]
    val leftTheme = left?.themes?.firstOrNull { it.id == themeId }
    val rightTheme = right?.themes?.firstOrNull { it.id == themeId }
    // Déplié par défaut : la comparaison se lit d'un seul défilement.
    var expanded by rememberSaveable(themeId) { mutableStateOf(true) }
    val chevronAngle by animateFloatAsState(if (expanded) 180f else 0f, label = "compare_chevron_$themeId")

    CompareCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button) { expanded = !expanded }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(meta?.emoji ?: "•", fontSize = 20.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                meta?.label ?: themeId,
                color = OnSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                modifier = Modifier.weight(1f),
            )
            CountChip(leftTheme?.measures?.size ?: 0, leftColor)
            if (hasRight) {
                Spacer(Modifier.width(4.dp))
                CountChip(rightTheme?.measures?.size ?: 0, rightColor)
            }
            Spacer(Modifier.width(4.dp))
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Replier" else "Déplier",
                tint = OnSurfaceMuted,
                modifier = Modifier.rotate(chevronAngle),
            )
        }

        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            SideBySide(
                left = { ThemeColumn(leftTheme, leftColor, hasProgramme = left != null) },
                right = {
                    if (hasRight) ThemeColumn(rightTheme, rightColor, hasProgramme = right != null)
                    else EmptyText("Choisissez un candidat à comparer")
                },
            )
        }
    }
}

@Composable
private fun CountChip(count: Int, color: Color) {
    val tint = if (count > 0) color else OnSurfaceMuted
    Text(
        text = "$count",
        color = tint,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(tint.copy(alpha = 0.16f))
            .padding(horizontal = 7.dp, vertical = 2.dp),
    )
}

@Composable
private fun ThemeColumn(theme: ProgrammeTheme?, color: Color, hasProgramme: Boolean) {
    if (!hasProgramme) {
        EmptyText("Programme bientôt disponible")
        return
    }
    if (theme == null || theme.measures.isEmpty()) {
        EmptyText("Aucune position publiée à ce jour")
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        theme.summary?.let {
            Text(it, color = OnSurfaceMuted, fontSize = 12.sp, fontStyle = FontStyle.Italic, lineHeight = 17.sp)
        }
        theme.measures.forEach { CompareMeasure(it, color) }
    }
}

@Composable
private fun CompareMeasure(measure: ProgrammeMeasure, color: Color) {
    val uriHandler = LocalUriHandler.current
    Row {
        Box(
            Modifier
                .padding(top = 7.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(7.dp))
        Column {
            Text(measure.text, color = OnSurface, fontSize = 13.sp, lineHeight = 18.sp)
            Spacer(Modifier.height(2.dp))
            val source = listOfNotNull(measure.sourceLabel, formatMonth(measure.date)).joinToString(" · ")
            Text(
                text = "${source.ifEmpty { "Source" }} ↗",
                color = OnSurfaceMuted,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable(role = Role.Button) {
                    runCatching { uriHandler.openUri(measure.sourceUrl) }
                },
            )
        }
    }
}

@Composable
private fun EmptyText(text: String) {
    Text(text, color = OnSurfaceMuted, fontSize = 12.sp, fontStyle = FontStyle.Italic, lineHeight = 17.sp)
}

@Composable
private fun CompareFooter(left: CandidateInfo?, leftProgramme: Programme?, right: CandidateInfo?, rightProgramme: Programme?) {
    val updates = listOfNotNull(
        left?.let { info -> formatDay(leftProgramme?.updatedAt)?.let { "${info.name} le $it" } },
        right?.let { info -> formatDay(rightProgramme?.updatedAt)?.let { "${info.name} le $it" } },
    )
    Text(
        text = buildString {
            if (updates.isNotEmpty()) append("Fiches mises à jour : ${updates.joinToString(", ")}. ")
            append("Synthèse réalisée à partir de sources publiques. Tous les candidats n'ont pas publié un programme ")
            append("aussi détaillé : un thème vide signifie qu'aucune position sourcée n'a été trouvée à ce jour. ")
            append("Consultez les sources pour le détail des propositions.")
        },
        color = OnSurfaceMuted,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        modifier = Modifier.padding(top = 12.dp, start = 4.dp, end = 4.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CandidatePickerSheet(
    title: String,
    selectedId: String?,
    excludedId: String?,
    onPick: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    // Ordre alphabétique des noms de famille (les ids sont "nom_prenom") : pas de hiérarchie implicite.
    val choices = remember(excludedId) {
        CANDIDATES.filter { it.id != excludedId }.sortedBy { it.id }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Surface,
    ) {
        Text(
            title,
            color = OnSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding(),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            items(choices, key = { it.id }) { info ->
                val color = parseHexColor(info.colorHex)
                val selected = info.id == selectedId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (selected) color.copy(alpha = 0.10f) else Color.Transparent)
                        .clickable(role = Role.Button) { onPick(info.id) }
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CandidateAvatar(info, color, size = 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(info.name, color = OnSurface, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        PartyChip(info.party, color)
                    }
                    if (selected) Text("✓", color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
