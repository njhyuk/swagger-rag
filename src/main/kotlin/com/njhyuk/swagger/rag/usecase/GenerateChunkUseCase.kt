package com.njhyuk.swagger.rag.usecase

import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import org.springframework.stereotype.Service

@Service
class GenerateChunkUseCase {
    fun generate(chunks: List<ApiDocChunk>): List<String> =
        chunks.map { chunk ->
            """
            ## [${chunk.method.uppercase()}] ${chunk.path}
            ${chunk.text}
            """.trimIndent()
        }
} 