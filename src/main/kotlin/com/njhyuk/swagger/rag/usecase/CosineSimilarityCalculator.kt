package com.njhyuk.swagger.rag.usecase

import kotlin.math.sqrt

object CosineSimilarityCalculator {
    fun similarity(a: List<Double>, b: List<Double>): Double {
        if (a.isEmpty() || b.isEmpty() || a.size != b.size) return -1.0
        val dot = a.zip(b).sumOf { (x, y) -> x * y }
        val normA = sqrt(a.sumOf { it * it })
        val normB = sqrt(b.sumOf { it * it })
        return if (normA == 0.0 || normB == 0.0) -1.0 else (dot / (normA * normB))
    }
} 