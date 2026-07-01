package com.applicationrush.elise2027.data.api

import com.applicationrush.elise2027.data.model.DeviceVoteResponse
import com.applicationrush.elise2027.data.model.VoteApiResponse
import com.applicationrush.elise2027.data.model.VoteCountResponse
import com.applicationrush.elise2027.data.model.VoteRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class VoteApi(private val baseUrl: String) {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(Logging) {
            level = LogLevel.BODY
        }
    }

    suspend fun getVotes(): VoteCountResponse =
        client.get("$baseUrl/votes").body()

    suspend fun getDeviceVote(phoneId: String): DeviceVoteResponse =
        client.get("$baseUrl/votes/$phoneId").body()

    suspend fun castVote(phoneId: String, candidateId: String, token: String): VoteApiResponse =
        client.post("$baseUrl/vote") {
            contentType(ContentType.Application.Json)
            setBody(VoteRequest(phoneId, candidateId, token))
        }.body()
}
