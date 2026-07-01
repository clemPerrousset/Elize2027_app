package com.applicationrush.elise2027.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.applicationrush.elise2027.BuildConfig
import com.applicationrush.elise2027.data.api.VoteApi
import com.applicationrush.elise2027.data.model.CANDIDATES
import com.applicationrush.elise2027.data.model.CandidateUiState
import com.applicationrush.elise2027.data.model.MOCK_VOTE_COUNTS
import com.applicationrush.elise2027.util.generateHmacToken
import com.applicationrush.elise2027.util.getPhoneId
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("elyze_prefs")

class VoteRepository(private val context: Context) {

    private val api = VoteApi(BuildConfig.SERVER_URL)

    private val KEY_ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
    private val KEY_VOTED_FOR = stringPreferencesKey("voted_for")

    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[KEY_ONBOARDING_DONE] ?: false }
    val votedCandidateId: Flow<String?> = context.dataStore.data.map { it[KEY_VOTED_FOR] }

    // Candidates hardcoded in app — always available regardless of server state
    fun baseUiList(votedId: String? = null): List<CandidateUiState> =
        buildUiList(emptyMap(), votedId)

    suspend fun markOnboardingDone() {
        context.dataStore.edit { it[KEY_ONBOARDING_DONE] = true }
    }

    suspend fun fetchVotes(): List<CandidateUiState> {
        val phoneId = getPhoneId(context)

        // Both calls are independent — if one fails we still show what we can
        val (votesResult, deviceVoteResult) = coroutineScope {
            val votes = async { runCatching { api.getVotes() } }
            val device = async { runCatching { api.getDeviceVote(phoneId) } }
            votes.await() to device.await()
        }

        val countMap = votesResult.getOrNull()
            ?.candidates
            ?.associate { it.id to it.count }
            ?: emptyMap()

        // Server vote wins; fall back to DataStore if server unreachable
        val votedId = deviceVoteResult.getOrNull()
            ?.candidate_id
            ?: context.dataStore.data.first()[KEY_VOTED_FOR]

        // Sync DataStore only when server responded
        if (deviceVoteResult.isSuccess) {
            context.dataStore.edit { prefs ->
                if (votedId != null) prefs[KEY_VOTED_FOR] = votedId
                else prefs.remove(KEY_VOTED_FOR)
            }
        }

        return buildUiList(countMap, votedId)
    }

    suspend fun vote(candidateId: String): String {
        val phoneId = getPhoneId(context)
        val token = generateHmacToken(phoneId, BuildConfig.HMAC_SECRET)
        val result = api.castVote(phoneId, candidateId, token)

        if (result.error != null) error(result.error)

        val newVoted = when (result.status) {
            "voted", "changed" -> candidateId
            "unvoted" -> null
            else -> null
        }
        context.dataStore.edit { prefs ->
            if (newVoted != null) prefs[KEY_VOTED_FOR] = newVoted
            else prefs.remove(KEY_VOTED_FOR)
        }
        return result.status ?: "unknown"
    }

    fun mockUiList(currentVotedId: String?): List<CandidateUiState> =
        buildUiList(MOCK_VOTE_COUNTS, currentVotedId)

    private fun buildUiList(
        countMap: Map<String, Int>,
        votedId: String? = null,
    ): List<CandidateUiState> {
        val maxCount = countMap.values.maxOrNull()?.takeIf { it > 0 } ?: 1
        return CANDIDATES
            .map { info ->
                val count = countMap[info.id] ?: 0
                CandidateUiState(
                    info = info,
                    voteCount = count,
                    progressFraction = count.toFloat() / maxCount,
                    isVotedFor = votedId == info.id,
                )
            }
            .sortedWith(compareByDescending<CandidateUiState> { it.voteCount }.thenBy { it.info.name })
    }
}
