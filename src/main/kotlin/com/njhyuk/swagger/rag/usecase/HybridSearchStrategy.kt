package com.njhyuk.swagger.rag.usecase

import com.njhyuk.swagger.rag.adapter.output.vectorstore.SearchResult
import com.njhyuk.swagger.rag.adapter.output.vectorstore.VectorStore
import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import org.springframework.stereotype.Component

@Component
class HybridSearchStrategy(
    private val vectorStore: VectorStore,
    private val openAI: OpenAI
) : AnswerStrategy {
    override suspend fun answer(query: String, chunks: List<ApiDocChunk>): String {
        // 1. 벡터 검색 결과 가져오기
        val vectorResults = vectorStore.search(query, limit = 5)
        
        // 2. LLM을 사용하여 쿼리의 의도 분석
        val queryIntent = analyzeQueryIntent(query)
        
        // 3. 텍스트 매칭 결과 가져오기 (LLM 분석 결과를 포함)
        val textResults = chunks.map { chunk ->
            val baseScore = TextMatchScoreCalculator.score(query, chunk.text)
            val semanticScore = calculateSemanticScore(queryIntent, chunk.text)
            SearchResult(
                chunk = chunk,
                score = (baseScore * 0.4) + (semanticScore * 0.6)
            )
        }.sortedByDescending { it.score }
            .take(5)

        // 4. 결과 통합 및 정렬
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

    private suspend fun analyzeQueryIntent(query: String): String {
        val prompt = """
            Analyze the following API search query and extract its main intent and related concepts.
            Focus on identifying the core action and domain.
            Query: "$query"
            
            Return the analysis in a concise format like:
            Action: [main action]
            Domain: [domain]
            Related terms: [comma-separated related terms]
        """.trimIndent()

        return callOpenAI(prompt)
    }

    private suspend fun calculateSemanticScore(queryIntent: String, apiDoc: String): Double {
        val prompt = """
            Compare the following API documentation with the query intent and return a relevance score between 0 and 1.
            
            Query Intent:
            $queryIntent
            
            API Documentation:
            $apiDoc
            
            Return only the score as a number between 0 and 1.
        """.trimIndent()

        return try {
            callOpenAI(prompt).toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            0.0
        }
    }

    private suspend fun callOpenAI(prompt: String): String {
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt
                )
            ),
            temperature = 0.3
        )

        val completion = openAI.chatCompletion(chatCompletionRequest)
        return completion.choices.firstOrNull()?.message?.content ?: ""
    }
} 