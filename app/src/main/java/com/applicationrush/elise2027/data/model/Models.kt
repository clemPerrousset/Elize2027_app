package com.applicationrush.elise2027.data.model

import kotlinx.serialization.Serializable

// ── Static local data ──────────────────────────────────────────────────────

data class CandidateInfo(
    val id: String,
    val name: String,
    val party: String,
    val bloc: String,
    val colorHex: String,
    val secondaryColorHex: String? = null,
    val photoUrl: String? = null,
)

// IDs must match what is registered on the backend via POST /candidates
private const val WC = "https://commons.wikimedia.org/wiki/Special:FilePath"

val CANDIDATES: List<CandidateInfo> = listOf(
    CandidateInfo("arthaud_nathalie",    "Nathalie Arthaud",       "Lutte ouvrière",      "Extrême gauche",  "#E2001A",
        photoUrl = "$WC/Nathalie_Arthaud_(LO)_19-05-2024.jpg"),
    CandidateInfo("asselineau_francois", "François Asselineau",    "UPR",                 "Souverainiste",   "#6B8E23",
        photoUrl = "$WC/Fran%C3%A7ois_ASSELINEAU.jpg"),
    CandidateInfo("melenchon_jeanluc",   "Jean-Luc Mélenchon",     "LFI",                 "Gauche",          "#7C3AED", secondaryColorHex = "#4C1D95",
        photoUrl = "$WC/Jean-Luc_MELENCHON_2016_2_(cropped).jpg"),
    CandidateInfo("tondelier_marine",    "Marine Tondelier",       "Les Écologistes",     "Gauche",          "#2E8B57",
        photoUrl = "$WC/Huma2023MarineTondelier.jpg"),
    CandidateInfo("guedj_jerome",        "Jérôme Guedj",           "PS",                  "Gauche",          "#FF8080",
        photoUrl = "$WC/J%C3%A9r%C3%B4me_Guedj_2010_(cropped).jpg"),
    CandidateInfo("bouamrane_karim",     "Karim Bouamrane",        "PS",                  "Gauche",          "#FF6B6B",
        photoUrl = "$WC/Portrait-maire-karim-bouamrane-saint-ouen.jpg"),
    CandidateInfo("ruffin_francois",     "François Ruffin",        "Debout !",            "Gauche",          "#B23A48",
        photoUrl = "$WC/Fran%C3%A7ois_Ruffin.jpg"),
    CandidateInfo("batho_delphine",      "Delphine Batho",         "Génération écologie", "Gauche",          "#4F9D69",
        photoUrl = "$WC/Delphine_Batho_p1370391.jpg"),
    CandidateInfo("glucksmann_raphael",  "Raphaël Glucksmann",     "Place publique",      "Gauche",          "#F4A300",
        photoUrl = "$WC/1720448398743_20240708_GLUCKSMANN_Raphael_FR_006.jpg"),
    CandidateInfo("hollande_francois",   "François Hollande",      "PS",                  "Gauche",          "#FFB3B3",
        photoUrl = "$WC/Fran%C3%A7ois_Hollande_-_2017_(27869823159).jpg"),
    CandidateInfo("faure_olivier",       "Olivier Faure",          "PS",                  "Gauche",          "#E84855",
        photoUrl = "$WC/DeputeXIVeLegVeRep-Olivier_Faure.jpg"),
    CandidateInfo("lucaslundy_benjamin", "Benjamin Lucas-Lundy",   "Génération·s",        "Gauche",          "#3CB371",
        photoUrl = "$WC/20210819_lucas.benjamin_5939.jpg"),
    CandidateInfo("massard_lydie",       "Lydie Massard",          "Génération·s",        "Gauche",          "#66CDAA",
        photoUrl = "$WC/Lydie_Massard_02-2024.jpg"),
    CandidateInfo("retailleau_bruno",    "Bruno Retailleau",       "LR",                  "Droite",          "#0066CC",
        photoUrl = "$WC/Bruno_Retailleau.png"),
    CandidateInfo("bertrand_xavier",     "Xavier Bertrand",        "Nous France",         "Droite",          "#1B4F8C",
        photoUrl = "$WC/Xavier_Bertrand_-_2023_(cropped).jpg"),
    CandidateInfo("dupontaignan_nicolas","Nicolas Dupont-Aignan",  "Debout la France",    "Droite",          "#003399",
        photoUrl = "$WC/Nicolas_Dupont-Aignan,_homme_politique_fran%C3%A7ais.jpg"),
    CandidateInfo("lisnard_david",       "David Lisnard",          "Nouvelle Énergie",    "Droite",          "#5DADE2",
        photoUrl = "$WC/David_Lisnard_-_2013.jpg"),
    CandidateInfo("wauquiez_laurent",    "Laurent Wauquiez",       "LR",                  "Droite",          "#002F6C",
        photoUrl = "$WC/Laurent_Wauquiez_2021.jpg"),
    CandidateInfo("attal_gabriel",       "Gabriel Attal",          "Renaissance",         "Centre",          "#FFD700",
        photoUrl = "$WC/Gabriel_Attal_2023_(cropped).jpg"),
    CandidateInfo("philippe_edouard",    "Édouard Philippe",       "Horizons",            "Centre",          "#7FB3D5",
        photoUrl = "$WC/%C3%89douard_Philippe_%C3%A0_Ch%C3%A2lons-en-Champagne_en_2023._(cropped).jpg"),
    CandidateInfo("darmanin_gerald",     "Gérald Darmanin",        "Renaissance / LR",    "Centre / Droite", "#C8A951",
        photoUrl = "$WC/Darmanin_2024_(cropped).jpg"),
    CandidateInfo("bardella_jordan",     "Jordan Bardella",        "RN",                  "Extrême droite",  "#04316A",
        photoUrl = "$WC/Jordan_Bardella_(cropped).jpg"),
    // ⚠️ Philippot : hébergé sur fr.wikipedia (pas Commons), à vérifier visuellement
    CandidateInfo("philippot_florian",   "Florian Philippot",      "Les Patriotes",       "Extrême droite",  "#5A6E8C",
        photoUrl = "https://fr.wikipedia.org/wiki/Special:FilePath/Florian_Philippot_(cropped).JPG"),
    CandidateInfo("zemmour_eric",        "Éric Zemmour",           "Reconquête",          "Extrême droite",  "#1A1A40",
        photoUrl = "$WC/Portrait_d'%C3%89ric_Zemmour,_avril_2022.jpg"),
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
