package com.njhyuk.swagger.rag.adapter.output.vectorstore

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.njhyuk.swagger.rag.adapter.output.embedding.OllamaEmbeddingClient
import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.UUID
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.HttpStatusCode
import org.springframework.beans.factory.annotation.Qualifier

@Component
class QdrantVectorStore(
    @Value("\${qdrant.host:localhost}") private val host: String,
    @Value("\${qdrant.port:6333}") private val port: Int,
    private val embeddingClient: OllamaEmbeddingClient,
    @Qualifier("qdrantHttpClient") private val httpClient: HttpClient
) : VectorStore {
    companion object {
        private const val COLLECTION_NAME = "api_docs"
        private const val VECTOR_SIZE = 768 // ollama nomic-embed-text embedding size
        private const val MAX_RETRIES = 3
        private const val SCORE_THRESHOLD = 0.5
    }

    private val baseUrl = "http://$host:$port"
    private val mapper: ObjectMapper = jacksonObjectMapper()

    init {
        runBlocking {
            initializeCollection()
        }
    }

    private suspend fun initializeCollection() {
        try {
            val checkResponse = httpClient.get("$baseUrl/collections/$COLLECTION_NAME")
            if (checkResponse.status == HttpStatusCode.NotFound) {
                createCollection()
            }
        } catch (e: Exception) {
            throw QdrantException("Error during collection initialization", e)
        }
    }

    private suspend fun createCollection() {
        val createResponse = httpClient.put("$baseUrl/collections/$COLLECTION_NAME") {
            contentType(ContentType.Application.Json)
            setBody(
                mapOf(
                    "vectors" to mapOf(
                        "size" to VECTOR_SIZE,
                        "distance" to "Cosine"
                    )
                )
            )
        }
        if (createResponse.status != HttpStatusCode.OK) {
            throw QdrantException("Failed to create collection: ${createResponse.status}")
        }
    }

    override fun store(chunks: List<ApiDocChunk>) {
        val points = chunks.map { chunk ->
            val embedding = chunk.embedding?.map { it.toFloat() } ?: emptyList()
            if (embedding.isEmpty()) {
                throw QdrantException("Empty embedding for chunk ${chunk.method} ${chunk.path}")
            }
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "vector" to embedding,
                "payload" to mapOf(
                    "path" to chunk.path,
                    "method" to chunk.method,
                    "text" to chunk.text
                )
            )
        }

        runBlocking {
            try {
                val response = httpClient.put("$baseUrl/collections/$COLLECTION_NAME/points") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("points" to points))
                }
                if (response.status != HttpStatusCode.OK) {
                    throw QdrantException("Failed to store points: ${response.status}")
                }
            } catch (e: Exception) {
                throw QdrantException("Error storing points in Qdrant", e)
            }
        }
    }

    override fun search(query: String, limit: Int): List<SearchResult> {
        val queryVector = embeddingClient.embed(query).map { it.toFloat() }
        var retryCount = 0

        while (true) {
            try {
                return runBlocking {
                    val response = httpClient.post("$baseUrl/collections/$COLLECTION_NAME/points/search") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            mapOf(
                                "vector" to queryVector,
                                "limit" to limit,
                                "score_threshold" to SCORE_THRESHOLD,
                                "with_payload" to true
                            )
                        )
                    }
                    parseSearchResponse(response)
                }
            } catch (e: Exception) {
                if (retryCount < MAX_RETRIES) {
                    retryCount++
                    Thread.sleep(1000L * retryCount) // Exponential backoff
                    continue
                }
                throw QdrantException("Error during vector search after $MAX_RETRIES retries", e)
            }
        }
    }

    private suspend fun parseSearchResponse(response: HttpResponse): List<SearchResult> {
        val responseText = response.bodyAsText()
        val searchResponse = mapper.readTree(responseText)

        if (!searchResponse.has("result") || searchResponse.get("result").isEmpty) {
            return emptyList()
        }

        return searchResponse.get("result").mapNotNull { result ->
            try {
                if (!result.has("payload") || result.get("payload").isNull) {
                    return@mapNotNull null
                }

                val payload = result.get("payload")
                if (!payload.has("path") || !payload.has("method") || !payload.has("text")) {
                    return@mapNotNull null
                }

                SearchResult(
                    chunk = ApiDocChunk(
                        path = payload.get("path").asText(),
                        method = payload.get("method").asText(),
                        text = payload.get("text").asText(),
                        embedding = null
                    ),
                    score = result.get("score").asDouble()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun clear() {
        try {
            runBlocking {
                val response = httpClient.delete("$baseUrl/collections/$COLLECTION_NAME")
                if (response.status == HttpStatusCode.OK) {
                    createCollection()
                } else {
                    throw QdrantException("Failed to clear collection: ${response.status}")
                }
            }
        } catch (e: Exception) {
            throw QdrantException("Error clearing Qdrant collection", e)
        }
    }
}