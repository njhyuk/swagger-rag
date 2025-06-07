package com.njhyuk.swagger.rag.service

import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import com.njhyuk.swagger.rag.usecase.QueryAnswerUseCase
import com.njhyuk.swagger.rag.infrastructure.ChunkRepository
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class ChatService(
    private val queryAnswerUseCase: QueryAnswerUseCase,
    private val chunkRepository: ChunkRepository
) {
    fun initialize(chunks: List<ApiDocChunk>) {
        chunkRepository.setChunks(chunks)
    }

    fun processQuestion(question: String): String {
        return runBlocking {
            queryAnswerUseCase.execute(question, chunkRepository.getChunks())
        }
    }
} 