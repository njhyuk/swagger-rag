package com.njhyuk.swagger.rag.usecase

import com.njhyuk.swagger.rag.adapter.output.embedding.OpenAIEmbeddingClient
import com.njhyuk.swagger.rag.adapter.output.vectorstore.QdrantVectorStore
import com.njhyuk.swagger.rag.adapter.output.vectorstore.VectorStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UseCaseConfig {
    @Bean
    fun answerStrategy(vectorStore: VectorStore): AnswerStrategy {
        return HybridSearchStrategy(vectorStore)
    }
} 