package com.njhyuk.swagger.rag.adapter.output.vectorstore

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.njhyuk.swagger.rag.adapter.output.embedding.OpenAIEmbeddingClient
import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.jackson.*
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.UUID
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.HttpStatusCode

@Component
class QdrantVectorStore(
    @Value("\${qdrant.host:localhost}") private val host: String,
    @Value("\${qdrant.port:6333}") private val port: Int,
    private val embeddingClient: OpenAIEmbeddingClient
) : VectorStore {
    private val collectionName = "api_docs"
    private val vectorSize = 1536 // OpenAI ada-002 embedding size
    private val baseUrl = "http://$host:$port"
    private val client: HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            jackson()
        }
    }
    private val mapper: ObjectMapper = jacksonObjectMapper()

    init {
        runBlocking {
            try {
                // Check if collection exists
                val checkResponse = client.get("$baseUrl/collections/$collectionName")
                if (checkResponse.status == HttpStatusCode.NotFound) {
                    // Create collection
                    val createResponse = client.put("$baseUrl/collections/$collectionName") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            mapOf(
                                "vectors" to mapOf(
                                    "size" to vectorSize,
                                    "distance" to "Cosine"
                                )
                            )
                        )
                    }
                    if (createResponse.status != HttpStatusCode.OK) {
                        throw RuntimeException("Failed to create collection: ${createResponse.status}")
                    }
                }
            } catch (e: Exception) {
                throw RuntimeException("Error during collection initialization", e)
            }
        }
    }

    override fun store(chunks: List<ApiDocChunk>) {
        val points = chunks.map { chunk ->
            val embedding = chunk.embedding?.map { it.toFloat() } ?: emptyList()
            if (embedding.isEmpty()) {
                throw RuntimeException("Empty embedding for chunk ${chunk.method} ${chunk.path}")
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
                val response = client.put("$baseUrl/collections/$collectionName/points") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("points" to points))
                }
                if (response.status != HttpStatusCode.OK) {
                    throw RuntimeException("Failed to store points: ${response.status}")
                }
            } catch (e: Exception) {
                throw RuntimeException("Error storing points in Qdrant", e)
            }
        }
    }

    override fun search(query: String, limit: Int): List<SearchResult> {
        val queryVector = embeddingClient.embed(query).map { it.toFloat() }

        return try {
            val response = runBlocking {
                client.post("$baseUrl/collections/$collectionName/points/search") {
                    contentType(ContentType.Application.Json)
                    setBody(
                        mapOf(
                            "vector" to queryVector,
                            "limit" to limit,
                            "score_threshold" to 0.5,
                            "with_payload" to true
                        )
                    )
                }
            }
            val responseText = runBlocking { response.bodyAsText() }
            val searchResponse = mapper.readTree(responseText)

            if (!searchResponse.has("result") || searchResponse.get("result").isEmpty) {
                return emptyList()
            }

            searchResponse.get("result").mapNotNull { result ->
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
        } catch (e: Exception) {
            throw RuntimeException("Error during vector search", e)
        }
    }

    override fun clear() {
        try {
            runBlocking {
                val response = client.delete("$baseUrl/collections/$collectionName")
                if (response.status == HttpStatusCode.OK) {
                    // Recreate the collection
                    val createResponse = client.put("$baseUrl/collections/$collectionName") {
                        contentType(ContentType.Application.Json)
                        setBody(
                            mapOf(
                                "vectors" to mapOf(
                                    "size" to vectorSize,
                                    "distance" to "Cosine"
                                )
                            )
                        )
                    }
                    if (createResponse.status != HttpStatusCode.OK) {
                        throw RuntimeException("Failed to recreate collection: ${createResponse.status}")
                    }
                } else {
                    throw RuntimeException("Failed to clear collection: ${response.status}")
                }
            }
        } catch (e: Exception) {
            throw RuntimeException("Error clearing Qdrant collection", e)
        }
    }
}