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
                
                // 파라미터 정보 수집
                val parameters = mutableListOf<String>()
                api["parameters"]?.forEach { param ->
                    val name = param["name"]?.asText() ?: ""
                    val desc = param["description"]?.asText() ?: ""
                    if (name.isNotBlank() && desc.isNotBlank()) {
                        parameters.add("- $name: $desc")
                    }
                }
                
                // 요청 본문 정보 수집
                val requestBody = api["requestBody"]?.let { body ->
                    val content = body["content"]?.get("application/json")
                    val schema = content?.get("schema")
                    if (schema != null) {
                        val required = schema["required"]?.map { it.asText() } ?: emptyList()
                        val properties = schema["properties"]?.fields()?.asSequence()?.map { (name, prop) ->
                            val desc = prop["description"]?.asText() ?: ""
                            val isRequired = required.contains(name)
                            "- $name${if (isRequired) " (필수)" else ""}: $desc"
                        }?.toList() ?: emptyList()
                        if (properties.isNotEmpty()) {
                            "\n**Request Body:**\n${properties.joinToString("\n")}"
                        } else ""
                    } else ""
                } ?: ""
                
                // 응답 정보 수집
                val responses = api["responses"]?.fields()?.asSequence()?.joinToString("\n") { (code, resp) ->
                    "[$code] ${resp["description"]?.asText() ?: ""}"
                } ?: ""
                
                // API 문서 텍스트 생성
                val text = buildString {
                    appendLine(summary)
                    if (description.isNotBlank()) appendLine(description)
                    if (parameters.isNotEmpty()) appendLine("\n**Parameters:**\n${parameters.joinToString("\n")}")
                    if (requestBody.isNotBlank()) appendLine(requestBody)
                    if (responses.isNotBlank()) appendLine("\n**Responses:**\n$responses")
                }.trim()
                
                // 임베딩 생성
                val embedding = embeddingClient.embed(text)
                result.add(ApiDocChunk(path, method, text, embedding))
            }
        }
        return result
    }
} 