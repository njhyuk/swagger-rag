package com.njhyuk.swagger.rag.adapter.output.embedding

import com.aallam.openai.api.embedding.EmbeddingRequest
import com.aallam.openai.client.OpenAI
import com.aallam.openai.api.model.ModelId
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Component

@Component
class OpenAIEmbeddingClient {
    private val apiKey: String = System.getenv("OPENAI_API_KEY")
        ?: throw IllegalStateException("OPENAI_API_KEY 환경변수가 설정되어 있지 않습니다.")
    private val openAI = OpenAI(apiKey)
    private val model = ModelId("text-embedding-ada-002")

    fun embed(text: String): List<Double> = runBlocking {
        val request = EmbeddingRequest(
            model = model,
            input = listOf(text)
        )
        val response = openAI.embeddings(request)
        response.embeddings.firstOrNull()?.embedding ?: emptyList()
    }
} 