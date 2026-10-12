package com.applicationrush.elise2027.data.model

import kotlinx.serialization.Serializable

// ── Programmes des candidats ───────────────────────────────────────────────
// Données embarquées dans assets/programmes.json, générées par programmes_data/merge.py
// (ne pas éditer à la main : modifier programmes_data/candidates/<id>.json puis relancer le script).

@Serializable
data class ProgrammesFile(
    val updatedAt: String,
    val programmes: List<Programme>,
)

@Serializable
data class Programme(
    val candidateId: String,
    val status: String,
    val statusNote: String? = null,
    val officialUrl: String? = null,
    val officialUrlLabel: String? = null,
    val officialUrlIsProgramme: Boolean = false,
    val programmeNote: String? = null,
    val highlights: List<String> = emptyList(),
    val themes: List<ProgrammeTheme> = emptyList(),
    val updatedAt: String? = null,
)

@Serializable
data class ProgrammeTheme(
    val id: String,
    val summary: String? = null,
    val measures: List<ProgrammeMeasure> = emptyList(),
)

@Serializable
data class ProgrammeMeasure(
    val text: String,
    val sourceUrl: String,
    val sourceLabel: String? = null,
    val date: String? = null,
)

data class ThemeInfo(val emoji: String, val label: String)

// Même liste et même ordre pour tous les candidats (neutralité + comparabilité).
val PROGRAMME_THEMES: Map<String, ThemeInfo> = mapOf(
    "economie"             to ThemeInfo("💶", "Économie & fiscalité"),
    "travail_retraites"    to ThemeInfo("👷", "Travail & retraites"),
    "social_sante"         to ThemeInfo("🏥", "Social & santé"),
    "education"            to ThemeInfo("🎓", "Éducation & jeunesse"),
    "ecologie_energie"     to ThemeInfo("🌱", "Écologie & énergie"),
    "securite_justice"     to ThemeInfo("🛡️", "Sécurité & justice"),
    "immigration"          to ThemeInfo("🛂", "Immigration"),
    "europe_international" to ThemeInfo("🌍", "Europe & international"),
    "institutions"         to ThemeInfo("🏛️", "Institutions & démocratie"),
)
