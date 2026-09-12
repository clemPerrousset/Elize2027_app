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
    CandidateInfo("melenchon_jeanluc",   "Jean-Luc Mélenchon",     "LFI",                 "Gauche",          "#C8285C", secondaryColorHex = "#7C3AED",
        photoUrl = "$WC/Jean-Luc_MELENCHON_2016_2_(cropped).jpg"),
    CandidateInfo("tondelier_marine",    "Marine Tondelier",       "Les Écologistes",     "Gauche",          "#2E8B57",
        photoUrl = "$WC/Huma2023MarineTondelier.jpg"),
    CandidateInfo("guedj_jerome",        "Jérôme Guedj",           "PS",                  "Gauche",          "#E91E63",
        photoUrl = "$WC/J%C3%A9r%C3%B4me_Guedj_2010_(cropped).jpg"),
    CandidateInfo("bouamrane_karim",     "Karim Bouamrane",        "PS",                  "Gauche",          "#FF6D00",
        photoUrl = "$WC/Portrait-maire-karim-bouamrane-saint-ouen.jpg"),
    CandidateInfo("ruffin_francois",     "François Ruffin",        "Debout !",            "Gauche",          "#B23A48",
        photoUrl = "$WC/Fran%C3%A7ois_Ruffin.jpg"),
    CandidateInfo("batho_delphine",      "Delphine Batho",         "Génération écologie", "Gauche",          "#4F9D69",
        photoUrl = "$WC/Delphine_Batho_p1370391.jpg"),
    CandidateInfo("glucksmann_raphael",  "Raphaël Glucksmann",     "Place publique",      "Gauche",          "#F4A300",
        photoUrl = "$WC/1720448398743_20240708_GLUCKSMANN_Raphael_FR_006.jpg"),
    CandidateInfo("faure_olivier",       "Olivier Faure",          "PS",                  "Gauche",          "#E84855",
        photoUrl = "$WC/DeputeXIVeLegVeRep-Olivier_Faure.jpg"),
    CandidateInfo("royal_segolene",      "Ségolène Royal",         "PS",                  "Gauche",          "#AD1457",
        photoUrl = "$WC/S%C3%A9gol%C3%A8ne_Royal_-_Janvier_2012.jpg"),
    CandidateInfo("massard_lydie",       "Lydie Massard",          "UDB",                 "Gauche",          "#66CDAA",
        photoUrl = "$WC/Lydie_Massard_02-2024.jpg"),
    CandidateInfo("kazib_anasse",        "Anasse Kazib",           "Révolution permanente","Extrême gauche", "#D84315",
        photoUrl = "$WC/Anasse_Kazib,_d%C3%A9cembre_2021.jpg"),
    // ⚠️ Labib : pas de portrait libre de droits trouvé sur Commons/Wikipedia — fallback initiales
    CandidateInfo("labib_selma",         "Selma Labib",            "NPA-Révolutionnaires","Extrême gauche",  "#8E24AA"),
    CandidateInfo("retailleau_bruno",    "Bruno Retailleau",       "LR",                  "Droite",          "#0066CC",
        photoUrl = "$WC/Bruno_Retailleau.png"),
    CandidateInfo("bertrand_xavier",     "Xavier Bertrand",        "Nous France",         "Droite",          "#1B4F8C",
        photoUrl = "$WC/Xavier_Bertrand_-_2023_(cropped).jpg"),
    CandidateInfo("dupontaignan_nicolas","Nicolas Dupont-Aignan",  "Debout la France",    "Droite",          "#003399",
        photoUrl = "$WC/Nicolas_Dupont-Aignan,_homme_politique_fran%C3%A7ais.jpg"),
    CandidateInfo("lisnard_david",       "David Lisnard",          "Nouvelle Énergie",    "Droite",          "#5DADE2",
        photoUrl = "$WC/David_Lisnard_-_2013.jpg"),
    CandidateInfo("attal_gabriel",       "Gabriel Attal",          "Renaissance",         "Centre",          "#FFD700",
        photoUrl = "$WC/Gabriel_Attal_2023_(cropped).jpg"),
    CandidateInfo("philippe_edouard",    "Édouard Philippe",       "Horizons",            "Centre",          "#7FB3D5",
        photoUrl = "$WC/%C3%89douard_Philippe_%C3%A0_Ch%C3%A2lons-en-Champagne_en_2023._(cropped).jpg"),
    CandidateInfo("lepen_marine",        "Marine Le Pen",          "RN",                  "Extrême droite",  "#1565C0",
        photoUrl = "$WC/Marine_Le_Pen_2025_(cropped).jpg"),
    // ⚠️ Philippot : hébergé sur fr.wikipedia (pas Commons), à vérifier visuellement
    CandidateInfo("philippot_florian",   "Florian Philippot",      "Les Patriotes",       "Extrême droite",  "#546E7A",
        photoUrl = "https://fr.wikipedia.org/wiki/Special:FilePath/Florian_Philippot_(cropped).JPG"),
    CandidateInfo("roussel_fabien",     "Fabien Roussel",     "PCF",                    "Gauche",        "#B71C1C"),
    CandidateInfo("branco_juan",        "Juan Branco",        "Les Ruches",             "Gauche",        "#F9A825", secondaryColorHex = "#212121"),
    CandidateInfo("egger_clara",        "Clara Egger",        "Solution démocratique", "Centre",        "#00ACC1"),
    CandidateInfo("brun_philippe",      "Philippe Brun",      "PS",                     "Gauche",        "#9575CD"),
    CandidateInfo("lalanne_francis",    "Francis Lalanne",    "France Libre",           "Souverainiste", "#00695C"),
    CandidateInfo("mathieu_benoit",     "Benoît Mathieu",     "Indépendant",            "Non classé",    "#757575"),
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

@Serializable
data class VoteHistoryResponse(val history: List<VoteHistoryPoint>)

@Serializable
data class VoteHistoryPoint(val date: Long, val candidate_id: String, val votes: Int)

// ── UI state ───────────────────────────────────────────────────────────────

data class CandidateUiState(
    val info: CandidateInfo,
    val voteCount: Int = 0,
    val progressFraction: Float = 0f,
    val isVotedFor: Boolean = false,
)

// ── Mock vote counts ───────────────────────────────────────────────────────

val MOCK_VOTE_COUNTS: Map<String, Int> = mapOf(
    "lepen_marine"         to 8500,
    "melenchon_jeanluc"    to 6000,
    "tondelier_marine"     to 6000, // tied 2nd with Mélenchon
    "attal_gabriel"        to 5200,
    "philippe_edouard"     to 4800,
    "glucksmann_raphael"   to 3500,
    "ruffin_francois"      to 2800,
    "retailleau_bruno"     to 2400,
    "bertrand_xavier"      to 1800,
    "philippot_florian"    to 900,
    "dupontaignan_nicolas" to 600,
    "royal_segolene"       to 520,
    "arthaud_nathalie"     to 450,
    "asselineau_francois"  to 320,
    "guedj_jerome"         to 280,
    "bouamrane_karim"      to 210,
    "faure_olivier"        to 150,
    "kazib_anasse"         to 130,
    "massard_lydie"        to 90,
    "batho_delphine"       to 70,
    "lisnard_david"        to 50,
    "labib_selma"          to 40,
)
