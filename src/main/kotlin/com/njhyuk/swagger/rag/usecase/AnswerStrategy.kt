package com.njhyuk.swagger.rag.usecase

import com.njhyuk.swagger.rag.domain.model.ApiDocChunk

interface AnswerStrategy {
    suspend fun answer(query: String, chunks: List<ApiDocChunk>): String
}