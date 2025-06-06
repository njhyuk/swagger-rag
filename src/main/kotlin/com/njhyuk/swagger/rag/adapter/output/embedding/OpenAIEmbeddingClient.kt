package com.njhyuk.swagger.rag.adapter.output.embedding

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.stereotype.Component

@Component
class OpenAIEmbeddingClient {
    private val apiKey: String = System.getenv("OPENAI_API_KEY")
        ?: throw IllegalStateException("OPENAI_API_KEY 환경변수가 설정되어 있지 않습니다.")
    private val client = OkHttpClient()
    private val objectMapper = ObjectMapper()
    private val endpoint = "https://api.openai.com/v1/embeddings"
    private val model = "text-embedding-ada-002"

    fun embed(text: String): List<Float> {
        val json = objectMapper.createObjectNode().apply {
            put("model", model)
            putArray("input").add(text)
        }.toString()
        val request = Request.Builder()
            .url(endpoint)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw RuntimeException("OpenAI API error: ${response.code} ${response.message}")
            val root: JsonNode = objectMapper.readTree(response.body?.string())
            val arr = root["data"]?.firstOrNull()?.get("embedding")
            return arr?.map { it.floatValue() } ?: emptyList()
        }
    }
} 