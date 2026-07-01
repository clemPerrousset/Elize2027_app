package com.applicationrush.elise2027.data.model

import kotlinx.serialization.Serializable

// ── Static local data ──────────────────────────────────────────────────────

data class CandidateInfo(
    val id: String,
    val name: String,
    val party: String,
    val bloc: String,
    val colorHex: String,
    val photoUrl: String? = null,
)

// IDs must match what is registered on the backend via POST /candidates
val CANDIDATES: List<CandidateInfo> = listOf(
    CandidateInfo("arthaud_nathalie",    "Nathalie Arthaud",       "Lutte ouvrière",      "Extrême gauche",  "#E2001A"),
    CandidateInfo("asselineau_francois", "François Asselineau",    "UPR",                 "Souverainiste",   "#6B8E23"),
    CandidateInfo("melenchon_jeanluc",   "Jean-Luc Mélenchon",     "LFI",                 "Gauche",          "#C9190E"),
    CandidateInfo("tondelier_marine",    "Marine Tondelier",       "Les Écologistes",     "Gauche",          "#2E8B57"),
    CandidateInfo("guedj_jerome",        "Jérôme Guedj",           "PS",                  "Gauche",          "#FF8080"),
    CandidateInfo("bouamrane_karim",     "Karim Bouamrane",        "PS",                  "Gauche",          "#FF6B6B"),
    CandidateInfo("ruffin_francois",     "François Ruffin",        "Debout !",            "Gauche",          "#B23A48"),
    CandidateInfo("batho_delphine",      "Delphine Batho",         "Génération écologie", "Gauche",          "#4F9D69"),
    CandidateInfo("glucksmann_raphael",  "Raphaël Glucksmann",     "Place publique",      "Gauche",          "#F4A300"),
    CandidateInfo("hollande_francois",   "François Hollande",      "PS",                  "Gauche",          "#FFB3B3"),
    CandidateInfo("faure_olivier",       "Olivier Faure",          "PS",                  "Gauche",          "#E84855"),
    CandidateInfo("lucaslundy_benjamin", "Benjamin Lucas-Lundy",   "Génération·s",        "Gauche",          "#3CB371"),
    CandidateInfo("massard_lydie",       "Lydie Massard",          "Génération·s",        "Gauche",          "#66CDAA"),
    CandidateInfo("retailleau_bruno",    "Bruno Retailleau",       "LR",                  "Droite",          "#0066CC"),
    CandidateInfo("bertrand_xavier",     "Xavier Bertrand",        "Nous France",         "Droite",          "#1B4F8C"),
    CandidateInfo("dupontaignan_nicolas","Nicolas Dupont-Aignan",  "Debout la France",    "Droite",          "#003399"),
    CandidateInfo("lisnard_david",       "David Lisnard",          "Nouvelle Énergie",    "Droite",          "#5DADE2"),
    CandidateInfo("wauquiez_laurent",    "Laurent Wauquiez",       "LR",                  "Droite",          "#002F6C"),
    CandidateInfo("attal_gabriel",       "Gabriel Attal",          "Renaissance",         "Centre",          "#FFD700"),
    CandidateInfo("philippe_edouard",    "Édouard Philippe",       "Horizons",            "Centre",          "#7FB3D5"),
    CandidateInfo("darmanin_gerald",     "Gérald Darmanin",        "Renaissance / LR",    "Centre / Droite", "#C8A951"),
    CandidateInfo("bardella_jordan",     "Jordan Bardella",        "RN",                  "Extrême droite",  "#04316A"),
    CandidateInfo("philippot_florian",   "Florian Philippot",      "Les Patriotes",       "Extrême droite",  "#5A6E8C"),
    CandidateInfo("zemmour_eric",        "Éric Zemmour",           "Reconquête",          "Extrême droite",  "#1A1A40"),
)

// ── API models ─────────────────────────────────────────────────────────────

@Serializable
data class VoteCountResponse(val candidates: List<CandidateCount>)

@Serializable
data class CandidateCount(val id: String, val count: Int)

@Serializable
data class VoteRequest(val phone_id: String, val candidate_id: String, val token: String)

@Serializable
data class VoteApiResponse(val status: String? = null, val error: String? = null)

@Serializable
data class DeviceVoteResponse(val candidate_id: String? = null)

// ── UI state ───────────────────────────────────────────────────────────────

data class CandidateUiState(
    val info: CandidateInfo,
    val voteCount: Int = 0,
    val progressFraction: Float = 0f,
    val isVotedFor: Boolean = false,
)

// ── Mock vote counts ───────────────────────────────────────────────────────

val MOCK_VOTE_COUNTS: Map<String, Int> = mapOf(
    "bardella_jordan"      to 8500,
    "melenchon_jeanluc"    to 6000,
    "tondelier_marine"     to 6000, // tied 2nd with Mélenchon
    "attal_gabriel"        to 5200,
    "philippe_edouard"     to 4800,
    "glucksmann_raphael"   to 3500,
    "ruffin_francois"      to 2800,
    "retailleau_bruno"     to 2400,
    "wauquiez_laurent"     to 2100,
    "bertrand_xavier"      to 1800,
    "zemmour_eric"         to 1200,
    "philippot_florian"    to 900,
    "darmanin_gerald"      to 750,
    "dupontaignan_nicolas" to 600,
    "arthaud_nathalie"     to 450,
    "asselineau_francois"  to 320,
    "guedj_jerome"         to 280,
    "bouamrane_karim"      to 210,
    "hollande_francois"    to 180,
    "faure_olivier"        to 150,
    "lucaslundy_benjamin"  to 120,
    "massard_lydie"        to 90,
    "batho_delphine"       to 70,
    "lisnard_david"        to 50,
)
