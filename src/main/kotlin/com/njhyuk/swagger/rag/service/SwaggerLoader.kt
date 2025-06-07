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

        val allChunks = mutableListOf<ApiDocChunk>()
        openapiDir.listFiles { file -> file.name.endsWith(".json") }?.forEach { file ->
            val chunks = parseSwaggerUseCase.parse(file)
            allChunks.addAll(chunks)
        }

        if (allChunks.isEmpty()) {
            throw IllegalStateException("로드된 API 문서가 없습니다.")
        }
        
        // 벡터 스토어에 청크 저장
        vectorStore.clear() // 기존 데이터 삭제
        vectorStore.store(allChunks)
        
        return allChunks
    }
} 