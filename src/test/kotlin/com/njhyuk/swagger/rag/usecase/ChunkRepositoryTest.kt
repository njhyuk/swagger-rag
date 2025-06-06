package com.njhyuk.swagger.rag.usecase

import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ChunkRepositoryTest {
    @Test
    fun `setChunks와 getChunks가 정상 동작`() {
        val repo = ChunkRepository()
        val chunks = listOf(
            ApiDocChunk("/a", "get", "text1"),
            ApiDocChunk("/b", "post", "text2")
        )
        repo.setChunks(chunks)
        val result = repo.getChunks()
        assertEquals(chunks, result)
    }
} 