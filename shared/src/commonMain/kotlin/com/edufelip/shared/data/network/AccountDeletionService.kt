package com.edufelip.shared.data.network

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

interface AccountDeletionService {
    suspend fun deleteAccount(idToken: String): Result<Unit>
}

class HttpAccountDeletionService(
    private val client: HttpClient,
    private val endpoint: String = AccountDeletionEndpoints.deleteAccountUrl,
) : AccountDeletionService {
    override suspend fun deleteAccount(idToken: String): Result<Unit> = runCatching {
        val response = client.post(endpoint) {
            header(HttpHeaders.Authorization, "Bearer $idToken")
            contentType(ContentType.Application.Json)
            setBody("{}")
        }
        if (!response.status.isSuccess()) {
            val body = response.bodyAsText()
            val message = parseError(body) ?: "Account deletion failed."
            error(message)
        }
    }
}

fun provideAccountDeletionService(): AccountDeletionService =
    HttpAccountDeletionService(NetworkClients.defaultHttpClient)

object AccountDeletionEndpoints {
    private const val region = "us-central1"
    private const val projectId = "amazing-note-7eb16"
    private const val functionName = "deleteAccount"
    val deleteAccountUrl: String = "https://$region-$projectId.cloudfunctions.net/$functionName"
}

private object NetworkClients {
    val defaultHttpClient: HttpClient by lazy { provideHttpClient() }
}

@Serializable
private data class ErrorPayload(val error: String? = null)

private val errorJson = Json { ignoreUnknownKeys = true }

private fun parseError(body: String): String? = body
    .takeIf { it.isNotBlank() }
    ?.let { raw ->
        runCatching { errorJson.decodeFromString(ErrorPayload.serializer(), raw).error }
            .getOrNull()
            ?.takeIf { it.isNotBlank() }
    }
