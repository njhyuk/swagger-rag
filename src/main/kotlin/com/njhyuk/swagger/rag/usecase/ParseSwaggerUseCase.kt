package com.njhyuk.swagger.rag.usecase

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.njhyuk.swagger.rag.adapter.output.embedding.OpenAIEmbeddingClient
import com.njhyuk.swagger.rag.domain.model.ApiDocChunk
import org.springframework.stereotype.Service
import java.io.File

@Service
class ParseSwaggerUseCase(
    private val objectMapper: ObjectMapper,
    private val embeddingClient: OpenAIEmbeddingClient
) {
    fun parse(file: File): List<ApiDocChunk> {
        val root: JsonNode = objectMapper.readTree(file)
        val paths = root["paths"] ?: return emptyList()
        val result = mutableListOf<ApiDocChunk>()
        for ((path, methods) in paths.fields()) {
            for ((method, api) in methods.fields()) {
                val summary = api["summary"]?.asText() ?: ""
                val description = api["description"]?.asText() ?: ""
                val parameters = api["parameters"]?.joinToString { p ->
                    "- ${p["name"]?.asText()}: ${p["description"]?.asText() ?: ""}"
                } ?: ""
                val responses = api["responses"]?.fields()?.asSequence()?.joinToString("\n") { (code, resp) ->
                    "[$code] ${resp["description"]?.asText() ?: ""}"
                } ?: ""
                val text = buildString {
                    appendLine("### $method $path")
                    appendLine(summary)
                    if (description.isNotBlank()) appendLine(description)
                    if (parameters.isNotBlank()) appendLine("\n**Parameters:**\n$parameters")
                    if (responses.isNotBlank()) appendLine("\n**Responses:**\n$responses")
                }.trim()
                val embedding = embeddingClient.embed(text)
                result.add(ApiDocChunk(path, method, text, embedding))
            }
        }
        return result
    }
} 