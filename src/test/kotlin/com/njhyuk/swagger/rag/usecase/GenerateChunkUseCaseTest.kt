package com.njhyuk.swagger.rag.usecase

import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GenerateChunkUseCaseTest {
    @Test
    fun `generate가 올바른 포맷의 문자열을 반환`() {
        val useCase = GenerateChunkUseCase()
        val chunks = listOf(
            ApiDocChunk("/a", "get", "text1"),
            ApiDocChunk("/b", "post", "text2")
        )
        val result = useCase.generate(chunks)
        assertEquals(
            listOf(
                "## [GET] /a\ntext1",
                "## [POST] /b\ntext2"
            ),
            result
        )
    }
} 