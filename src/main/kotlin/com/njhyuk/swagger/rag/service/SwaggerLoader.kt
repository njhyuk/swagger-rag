package com.njhyuk.swagger.rag.service

import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import com.njhyuk.swagger.rag.usecase.ParseSwaggerUseCase
import org.springframework.stereotype.Service
import java.io.File

@Service
class SwaggerLoader(
    private val parseSwaggerUseCase: ParseSwaggerUseCase
) {
    fun loadAll(): List<ApiDocChunk> {
        val openapiDir = File("openapi")
        if (!openapiDir.exists() || !openapiDir.isDirectory) {
            throw IllegalStateException("openapi 디렉토리가 존재하지 않습니다.")
        }

        val allChunks = mutableListOf<ApiDocChunk>()
        openapiDir.listFiles { file -> file.name.endsWith(".json") }?.forEach { file ->
            println("Loading ${file.name}...")
            val chunks = parseSwaggerUseCase.parse(file)
            println("Found ${chunks.size} API endpoints in ${file.name}")
            chunks.forEach { chunk ->
                println("  - [${chunk.method.uppercase()}] ${chunk.path}")
            }
            allChunks.addAll(chunks)
        }

        if (allChunks.isEmpty()) {
            throw IllegalStateException("로드된 API 문서가 없습니다.")
        }

        println("\nTotal loaded APIs: ${allChunks.size}")
        return allChunks
    }
} 