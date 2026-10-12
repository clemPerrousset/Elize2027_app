package com.applicationrush.elise2027.data.repository

import android.content.Context
import android.util.Log
import com.applicationrush.elise2027.data.model.Programme
import com.applicationrush.elise2027.data.model.ProgrammesFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

// Programmes 100 % locaux (assets/programmes.json) : aucun appel serveur.
// Le fichier (~250 Ko) est décodé une seule fois puis gardé en mémoire.
object ProgrammeRepository {
    private val json = Json { ignoreUnknownKeys = true }

    @Volatile
    private var cache: Map<String, Programme>? = null

    suspend fun get(context: Context, candidateId: String): Programme? = withContext(Dispatchers.IO) {
        val all = cache ?: load(context).also { cache = it }
        all[candidateId]
    }

    private fun load(context: Context): Map<String, Programme> = runCatching {
        context.assets.open("programmes.json").bufferedReader().use { reader ->
            json.decodeFromString<ProgrammesFile>(reader.readText())
        }.programmes.associateBy { it.candidateId }
    }.getOrElse { e ->
        Log.e("ProgrammeRepository", "Lecture de programmes.json impossible", e)
        emptyMap()
    }
}
