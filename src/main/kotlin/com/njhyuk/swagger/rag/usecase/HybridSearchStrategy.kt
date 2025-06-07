package com.njhyuk.swagger.rag.usecase

import com.njhyuk.swagger.rag.adapter.output.vectorstore.SearchResult
import com.njhyuk.swagger.rag.adapter.output.vectorstore.VectorStore
import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import org.springframework.stereotype.Component

@Component
class HybridSearchStrategy(
    private val vectorStore: VectorStore
) : AnswerStrategy {
    override fun answer(query: String, chunks: List<ApiDocChunk>): String {
        // 벡터 검색 결과 가져오기
        val vectorResults = vectorStore.search(query, limit = 3)
        
        // 텍스트 매칭 결과 가져오기
        val textResults = chunks.map { chunk ->
            SearchResult(
                chunk = chunk,
                score = TextMatchScoreCalculator.score(query, chunk.text).toDouble()
            )
        }.sortedByDescending { it.score }
            .take(3)

        // 결과 통합 및 정렬
        val combinedResults = (vectorResults + textResults)
            .distinctBy { it.chunk.path + it.chunk.method }
            .sortedByDescending { it.score }
            .take(3)

        // 결과가 없으면 기본 메시지 반환
        if (combinedResults.isEmpty()) {
            return "No relevant API found."
        }

        // 결과 포맷팅
        return buildString {
            appendLine("Found ${combinedResults.size} relevant APIs:")
            combinedResults.forEachIndexed { index, result ->
                appendLine("\n${index + 1}. [${result.chunk.method.uppercase()}] ${result.chunk.path}")
                appendLine("   ${result.chunk.text}")
                appendLine("   Relevance score: ${String.format("%.2f", result.score)}")
            }
        }
    }
} 