package com.njhyuk.swagger.rag.usecase

import com.njhyuk.swagger.rag.domain.model.ApiDocChunk

interface AnswerStrategy {
    fun answer(query: String, chunks: List<ApiDocChunk>): String
}

class EmbeddingAnswerStrategy(
    private val embeddingClient: com.njhyuk.swagger.rag.adapter.output.embedding.OpenAIEmbeddingClient,
    private val fallback: AnswerStrategy = TextMatchAnswerStrategy()
) : AnswerStrategy {
    override fun answer(query: String, chunks: List<ApiDocChunk>): String {
        val queryEmbedding = embeddingClient.embed(query)
        val best = chunks.filter { it.embedding != null }
            .maxByOrNull { chunk ->
                CosineSimilarityCalculator.similarity(queryEmbedding, chunk.embedding!!)
            }
        return best?.text ?: fallback.answer(query, chunks)
    }
}

class TextMatchAnswerStrategy : AnswerStrategy {
    override fun answer(query: String, chunks: List<ApiDocChunk>): String {
        val best = chunks.maxByOrNull { chunk ->
            TextMatchScoreCalculator.score(query, chunk.text)
        }
        return best?.text ?: "No relevant API found."
    }
} 