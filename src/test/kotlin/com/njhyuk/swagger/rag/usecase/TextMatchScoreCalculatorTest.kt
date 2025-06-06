package com.njhyuk.swagger.rag.usecase

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TextMatchScoreCalculatorTest {
    @Test
    fun `정확히 포함될 때 높은 점수 반환`() {
        val q = "hello"
        val doc = "this is a hello world"
        val result = TextMatchScoreCalculator.score(q, doc)
        assertEquals(1005, result)
    }

    @Test
    fun `단어 일부만 포함될 때 각 단어 길이 합 반환`() {
        val q = "hello world"
        val doc = "say hello to the world"
        val result = TextMatchScoreCalculator.score(q, doc)
        assertEquals(10, result)
    }

    @Test
    fun `포함되지 않을 때 0 반환`() {
        val q = "foo bar"
        val doc = "baz qux"
        val result = TextMatchScoreCalculator.score(q, doc)
        assertEquals(0, result)
    }
} 