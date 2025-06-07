package com.njhyuk.swagger.rag.service

import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import com.njhyuk.swagger.rag.usecase.ParseSwaggerUseCase
import com.njhyuk.swagger.rag.adapter.output.vectorstore.VectorStore
import org.springframework.stereotype.Service
import java.io.File

@Service
class SwaggerLoader(
    private val parseSwaggerUseCase: ParseSwaggerUseCase,
    private val vectorStore: VectorStore
) {
    fun loadAll(): List<ApiDocChunk> {
        val openapiDir = File("openapi")
        if (!openapiDir.exists() || !openapiDir.isDirectory) {
            throw IllegalStateException("openapi 디렉토리가 존재하지 않습니다.")
        }

        println("Loading Swagger files from ${openapiDir.absolutePath}")
        val allChunks = mutableListOf<ApiDocChunk>()
        openapiDir.listFiles { file -> file.name.endsWith(".json") }?.forEach { file ->
            println("Loading ${file.name}...")
            val chunks = parseSwaggerUseCase.parse(file)
            println("Found ${chunks.size} API endpoints in ${file.name}")
            chunks.forEach { chunk ->
                println("  - [${chunk.method.uppercase()}] ${chunk.path}")
                if (chunk.embedding == null) {
                    println("    Warning: No embedding generated for this chunk")
                } else {
                    println("    Embedding size: ${chunk.embedding.size}")
                }
            }
            allChunks.addAll(chunks)
        }

        if (allChunks.isEmpty()) {
            throw IllegalStateException("로드된 API 문서가 없습니다.")
        }

        println("\nTotal loaded APIs: ${allChunks.size}")
        println("Storing chunks in vector store...")
        
        // 벡터 스토어에 청크 저장
        vectorStore.clear() // 기존 데이터 삭제
        vectorStore.store(allChunks)
        
        println("Chunks stored successfully")
        return allChunks
    }
} 