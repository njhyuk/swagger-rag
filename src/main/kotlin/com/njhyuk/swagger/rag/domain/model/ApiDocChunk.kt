package com.njhyuk.swagger.rag.domain.model

data class ApiDocChunk(
    val path: String,
    val method: String,
    val text: String,
    val embedding: List<Float>? = null
) 