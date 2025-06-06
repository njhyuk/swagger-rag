package com.njhyuk.swagger.rag.usecase

import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import org.springframework.stereotype.Service

@Service
class QueryAnswerUseCase(
    private val chunkRepository: ChunkRepository,
    private val answerStrategy: AnswerStrategy
) {
    fun answer(query: String): String {
        val chunks = chunkRepository.getChunks()
        if (chunks.isEmpty()) return "No API docs loaded."
        return answerStrategy.answer(query, chunks)
    }
} 