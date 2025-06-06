package com.njhyuk.swagger.rag.usecase

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CosineSimilarityCalculatorTest {
    @Test
    fun `동일한 벡터는 1을 반환한다`() {
        val a = listOf(1.0, 2.0, 3.0)
        val b = listOf(1.0, 2.0, 3.0)
        val result = CosineSimilarityCalculator.similarity(a, b)
        assertEquals(1.0, result, 1e-9)
    }

    @Test
    fun `직교 벡터는 0을 반환한다`() {
        val a = listOf(1.0, 0.0)
        val b = listOf(0.0, 1.0)
        val result = CosineSimilarityCalculator.similarity(a, b)
        assertEquals(0.0, result, 1e-9)
    }

    @Test
    fun `길이가 다르거나 비어있으면 -1을 반환한다`() {
        val a = listOf(1.0, 2.0)
        val b = listOf(1.0)
        val result = CosineSimilarityCalculator.similarity(a, b)
        assertEquals(-1.0, result, 1e-9)
        val empty = listOf<Double>()
        assertEquals(-1.0, CosineSimilarityCalculator.similarity(a, empty), 1e-9)
    }
} 