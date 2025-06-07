package com.njhyuk.swagger.rag.adapter.input.web

import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import com.njhyuk.swagger.rag.usecase.QueryAnswerUseCase
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/query")
class QueryController(
    private val queryAnswerUseCase: QueryAnswerUseCase
) {
    @PostMapping
    suspend fun query(
        @RequestBody request: QueryRequest,
        @RequestParam chunks: List<ApiDocChunk>
    ): String {
        return queryAnswerUseCase.execute(request.query, chunks)
    }
}

data class QueryRequest(
    val query: String
) 