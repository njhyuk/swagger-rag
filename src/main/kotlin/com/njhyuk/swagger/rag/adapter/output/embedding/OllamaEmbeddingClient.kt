package com.njhyuk.swagger.rag.adapter.output.embedding

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.beans.factory.annotation.Qualifier
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

@Component
class OllamaEmbeddingClient(
    @Value("\${ollama.host:localhost}") private val host: String,
    @Value("\${ollama.port:11434}") private val port: Int,
    @Qualifier("ollamaHttpClient") private val httpClient: HttpClient
) {
    private val baseUrl = "http://$host:$port"

    fun embed(text: String): List<Double> = runBlocking {
        val response = httpClient.post("$baseUrl/api/embeddings") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "model" to "nomic-embed-text",
                "prompt" to text
            ))
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw IllegalStateException("Failed to get embeddings from Ollama: ${response.status}")
        }

        val responseBody = response.bodyAsText()
        val embedding = jacksonObjectMapper()
            .readTree(responseBody)["embedding"]
            .map { it.asDouble() }
        embedding
    }
} 