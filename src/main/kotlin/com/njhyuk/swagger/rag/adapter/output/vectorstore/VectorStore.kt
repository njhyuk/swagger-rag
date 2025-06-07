package com.njhyuk.swagger.rag.adapter.output.vectorstore

import com.njhyuk.swagger.rag.domain.model.ApiDocChunk

interface VectorStore {
    fun store(chunks: List<ApiDocChunk>)
    fun search(query: String, limit: Int = 5): List<SearchResult>
    fun clear()
}

data class SearchResult(
    val chunk: ApiDocChunk,
    val score: Double
) 