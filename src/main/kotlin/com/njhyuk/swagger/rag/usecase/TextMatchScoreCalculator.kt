package com.njhyuk.swagger.rag.usecase

object TextMatchScoreCalculator {
    fun score(q: String, doc: String): Int {
        val ql = q.lowercase()
        val dl = doc.lowercase()
        return if (dl.contains(ql)) 1000 + q.length else ql.split(" ").sumOf { w ->
            if (dl.contains(w)) w.length else 0
        }
    }
} 