package com.njhyuk.swagger.rag.usecase

import com.njhyuk.swagger.rag.adapter.output.embedding.OpenAIEmbeddingClient
import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.sqrt

@Service
class QueryAnswerUseCase(
    private val embeddingClient: OpenAIEmbeddingClient
) {
    private val chunksRef = AtomicReference<List<ApiDocChunk>>(emptyList())

    fun loadChunks(chunks: List<ApiDocChunk>) {
        chunksRef.set(chunks)
    }

    fun answer(query: String): String {
        val chunks = chunksRef.get()
        if (chunks.isEmpty()) return "No API docs loaded."
        val queryEmbedding = embeddingClient.embed(query)
        val best = chunks
            .filter { it.embedding != null }
            .maxByOrNull { chunk ->
                cosineSimilarity(queryEmbedding, chunk.embedding!!)
            }
        return best?.text ?: fallbackAnswer(query, chunks)
    }

    private fun cosineSimilarity(a: List<Float>, b: List<Float>): Float {
        if (a.isEmpty() || b.isEmpty() || a.size != b.size) return -1f
        val dot = a.zip(b).sumOf { (x, y) -> x * y.toDouble() }.toFloat()
        val normA = sqrt(a.sumOf { it.toDouble() * it.toDouble() }).toFloat()
        val normB = sqrt(b.sumOf { it.toDouble() * it.toDouble() }).toFloat()
        return if (normA == 0f || normB == 0f) -1f else (dot / (normA * normB))
    }

    private fun fallbackAnswer(query: String, chunks: List<ApiDocChunk>): String {
        // 임베딩이 없는 경우 기존 텍스트 기반 매칭
        val best = chunks.maxByOrNull { chunk ->
            score(query, chunk.text)
        }
        return best?.text ?: "No relevant API found."
    }

    private fun score(q: String, doc: String): Int {
        val ql = q.lowercase()
        val dl = doc.lowercase()
        return if (dl.contains(ql)) 1000 + q.length else ql.split(" ").sumOf { w ->
            if (dl.contains(w)) w.length else 0
        }
    }
} 