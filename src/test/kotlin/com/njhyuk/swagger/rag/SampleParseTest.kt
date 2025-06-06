package com.njhyuk.swagger.rag

import com.fasterxml.jackson.databind.ObjectMapper
import com.njhyuk.swagger.rag.usecase.ParseSwaggerUseCase
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class SampleParseTest {
    @Test
    fun `swagger 샘플 파싱`() {
        val useCase = ParseSwaggerUseCase(ObjectMapper())
        val file = File("openapi/sample-api.json")
        val chunks = useCase.parse(file)
        assertTrue(chunks.isNotEmpty())
    }
} 