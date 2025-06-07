package com.njhyuk.swagger.rag.usecase

import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import org.springframework.stereotype.Service

@Service
class QueryAnswerUseCase(
    private val answerStrategy: AnswerStrategy
) {
    suspend fun execute(query: String, chunks: List<ApiDocChunk>): String {
        return answerStrategy.answer(query, chunks)
    }
} 