package com.njhyuk.swagger.rag.usecase

import com.njhyuk.swagger.rag.adapter.output.vectorstore.VectorStore
import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import org.springframework.stereotype.Component

@Component
class HybridSearchStrategy(
    private val vectorStore: VectorStore,
) : AnswerStrategy {
    override suspend fun answer(query: String, chunks: List<ApiDocChunk>): String {
        // 벡터 검색 수행
        val results = vectorStore.search(query, limit = 3)

        // 결과가 없으면 기본 메시지 반환
        if (results.isEmpty()) {
            return "No relevant API found."
        }

        // 결과 포맷팅
        return buildString {
            appendLine("Found ${results.size} relevant APIs:")
            results.forEachIndexed { index, result ->
                appendLine("\n${index + 1}. [${result.chunk.method.uppercase()}] ${result.chunk.path}")
                appendLine("   ${result.chunk.text}")
                appendLine("   Relevance score: ${String.format("%.2f", result.score)}")
            }
        }
    }
} 