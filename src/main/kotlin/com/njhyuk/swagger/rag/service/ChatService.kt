package com.njhyuk.swagger.rag.service

import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import com.njhyuk.swagger.rag.usecase.QueryAnswerUseCase
import com.njhyuk.swagger.rag.usecase.ChunkRepository
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
        return queryAnswerUseCase.answer(question)
    }

    fun getLoadedApiCount(): Int {
        return chunkRepository.getChunks().size
    }
} 