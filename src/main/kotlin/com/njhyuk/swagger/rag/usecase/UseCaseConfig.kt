package com.njhyuk.swagger.rag.usecase

import com.njhyuk.swagger.rag.adapter.output.embedding.OpenAIEmbeddingClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UseCaseConfig {
    @Bean
    fun answerStrategy(embeddingClient: OpenAIEmbeddingClient): AnswerStrategy {
        return EmbeddingAnswerStrategy(embeddingClient)
    }
} 