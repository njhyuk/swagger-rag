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

@Component
class QdrantVectorStore(
    @Value("\${qdrant.host:localhost}") private val host: String,
    @Value("\${qdrant.port:6333}") private val port: Int,
    private val embeddingClient: OpenAIEmbeddingClient
) : VectorStore {
    private val collectionName = "api_docs"
    private val vectorSize = 1536 // OpenAI ada-002 embedding size
    private val baseUrl = "http://$host:$port"
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            jackson()
        }
    }
    private val mapper = jacksonObjectMapper()

    init {
        // 컬렉션 생성
        runBlocking {
            client.put("$baseUrl/collections/$collectionName") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "vectors" to mapOf(
                        "size" to vectorSize,
                        "distance" to "Cosine"
                    )
                ))
            }
        }
    }

    override fun store(chunks: List<ApiDocChunk>) {
        val points = chunks.map { chunk ->
            mapOf(
                "id" to UUID.randomUUID().toString(),
                "vector" to (chunk.embedding?.map { it.toFloat() } ?: emptyList()),
                "payload" to mapOf(
                    "path" to chunk.path,
                    "method" to chunk.method,
                    "text" to chunk.text
                )
            )
        }

        runBlocking {
            client.put("$baseUrl/collections/$collectionName/points") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("points" to points))
            }
        }
    }

    override fun search(query: String, limit: Int): List<SearchResult> {
        val queryVector = embeddingClient.embed(query).map { it.toFloat() }
        
        return try {
            val response = runBlocking {
                client.post("$baseUrl/collections/$collectionName/points/search") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf(
                        "vector" to queryVector,
                        "limit" to limit
                    ))
                }
            }
            val responseText = runBlocking { response.bodyAsText() }
            val searchResponse = mapper.readTree(responseText)
            
            if (!searchResponse.has("result") || searchResponse.get("result").isEmpty) {
                return emptyList()
            }
            
            searchResponse.get("result").map { result ->
                val payload = result.get("payload")
                SearchResult(
                    chunk = ApiDocChunk(
                        path = payload.get("path").asText(),
                        method = payload.get("method").asText(),
                        text = payload.get("text").asText(),
                        embedding = null
                    ),
                    score = result.get("score").asDouble()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun clear() {
        try {
            runBlocking {
                client.delete("$baseUrl/collections/$collectionName")
            }
        } catch (e: Exception) {
            // 컬렉션이 없는 경우 무시
        }
    }
} 