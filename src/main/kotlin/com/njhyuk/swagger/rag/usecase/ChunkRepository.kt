package com.njhyuk.swagger.rag.usecase

import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicReference

@Component
class ChunkRepository {
    private val chunksRef = AtomicReference<List<ApiDocChunk>>(emptyList())

    fun setChunks(chunks: List<ApiDocChunk>) {
        chunksRef.set(chunks)
    }

    fun getChunks(): List<ApiDocChunk> = chunksRef.get()
} 