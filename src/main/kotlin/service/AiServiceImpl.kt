package com.alfoll.service

import com.alfoll.database.repository.AiRepository
import com.alfoll.exception.OpenRouterException
import com.alfoll.exception.RecordNotFoundException
import com.alfoll.model.AiResponseDTO
import com.alfoll.model.RewriteRequestDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.net.URI
import java.net.http.HttpClient
import io.ktor.http.HttpHeaders
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDateTime

class AiServiceImpl(
    private val aiRepository: AiRepository,
    private val aiUrl: String,
    private val apiKey: String,
    private val model: String
): AiService {

    // Http клиент - отправляет запрос
    private val httpClient = HttpClient.newBuilder().build()

    // создается тело запроса
    private fun buildJsonBody(text: String): String {
        return buildJsonObject {
            put("model", model)
            putJsonArray("messages") {
                addJsonObject {
                    put("role", "user")
                    put("content", text)
                }
            }
        }.toString()
    }

    // отправить запрос на open router
    private suspend fun openRouterRequest(text: String): String {
        return withContext(Dispatchers.IO) { // в джаве блокирующий send()
            try {
                val body = buildJsonBody(text)
                val request = HttpRequest.newBuilder()
                    .uri(URI.create(aiUrl))
                    .header(HttpHeaders.Authorization, "Bearer $apiKey")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build()

                val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

                if (response.statusCode() !in 200..299) {
                    val error = try {
                        val errorJson = Json.parseToJsonElement(response.body()).jsonObject
                        errorJson["error"]
                            ?.jsonObject
                            ?.get("message")
                            ?.jsonPrimitive
                            ?.content
                    } catch (_: Exception) {
                        null
                    }
                    throw OpenRouterException("OpenRouter HTTP ${response.statusCode()}: ${error ?: "Unknown error"}")
                }

                val ans = Json.parseToJsonElement(response.body()).jsonObject

                return@withContext ans["choices"]
                    ?.jsonArray
                    ?.firstOrNull()
                    ?.jsonObject
                    ?.get("message")
                    ?.jsonObject
                    ?.get("content")
                    ?.jsonPrimitive
                    ?.content
                    ?: throw OpenRouterException("No response from Open router")
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
                throw OpenRouterException("Request to OpenRouter was interrupted")
            } catch (e: IOException) {
                throw OpenRouterException("Network error while calling OpenRouter: ${e.message}")
            } catch (_: SerializationException) {
                throw OpenRouterException("Invalid JSON")
            }
        }
    }

    override suspend fun rewrite(text: RewriteRequestDTO): AiResponseDTO {
        require(text.text.isNotBlank()) { "Text cannot be empty" } // бросает IllegalArgumentException

        val aiResponse = openRouterRequest(text.text) // ии ответ
        val createdAt = LocalDateTime.now()

        return aiRepository.create(
            text.text,
            aiResponse,
            createdAt.toString()
        )
    }

    override suspend fun getHistory(): List<AiResponseDTO> {
        return aiRepository.getAll()
    }

    override suspend fun getRecord(id: Int): AiResponseDTO {
        require(id > 0 ) { "Id must be positive" } // бросает IllegalArgumentException

        return aiRepository.getById(id)
            ?: throw RecordNotFoundException("Record not found")
    }

    override suspend fun deleteRecord(id: Int) {
        require(id > 0) { "Id must be positive" }
        val deleted = aiRepository.deleteById(id)

        if (!deleted)
            throw RecordNotFoundException("Record not found")
    }
}