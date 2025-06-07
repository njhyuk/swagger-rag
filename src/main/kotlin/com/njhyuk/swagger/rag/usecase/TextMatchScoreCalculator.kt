package com.njhyuk.swagger.rag.usecase

object TextMatchScoreCalculator {
    fun score(q: String, doc: String): Double {
        val ql = q.lowercase()
        val dl = doc.lowercase()
        
        // 정확한 문구 매칭
        if (dl.contains(ql)) {
            return 1000.0 + q.length
        }
        
        // 단어 단위 매칭
        val queryWords = ql.split(" ").filter { it.length > 1 }
        if (queryWords.isEmpty()) return 0.0
        
        var totalScore = 0.0
        var matchedWords = 0
        
        for (word in queryWords) {
            if (dl.contains(word)) {
                // 단어 길이에 비례하는 점수
                val wordScore = word.length * 10.0
                // 연속된 단어 매칭에 대한 보너스
                val bonus = if (matchedWords > 0) 20.0 else 0.0
                totalScore += wordScore + bonus
                matchedWords++
            }
        }
        
        // 매칭된 단어 비율에 따른 보너스
        val matchRatio = matchedWords.toDouble() / queryWords.size
        val ratioBonus = if (matchRatio > 0.5) 200.0 else 0.0
        
        return totalScore + ratioBonus
    }
} 