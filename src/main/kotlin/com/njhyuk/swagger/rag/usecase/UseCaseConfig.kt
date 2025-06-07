package com.njhyuk.swagger.rag.usecase

import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.njhyuk.swagger.rag.adapter.output.embedding.OpenAIEmbeddingClient
import com.njhyuk.swagger.rag.adapter.output.vectorstore.QdrantVectorStore
import com.njhyuk.swagger.rag.adapter.output.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class UseCaseConfig {
    @Value("\${openai.api.key}")
    private lateinit var apiKey: String

    @Value("\${qdrant.host:localhost}")
    private lateinit var qdrantHost: String

    @Value("\${qdrant.port:6333}")
    private var qdrantPort: Int = 6333

    @Bean
    fun openAI(): OpenAI {
        val config = OpenAIConfig(
            token = apiKey,
            logging = LoggingConfig(logLevel = LogLevel.None),
        )
        return OpenAI(config)
    }

    @Bean
    fun answerStrategy(embeddingClient: OpenAIEmbeddingClient): AnswerStrategy {
        return HybridSearchStrategy(
            vectorStore = vectorStore(embeddingClient),
        )
    }

    @Bean
    fun vectorStore(embeddingClient: OpenAIEmbeddingClient): VectorStore {
        return QdrantVectorStore(
            host = qdrantHost,
            port = qdrantPort,
            embeddingClient = embeddingClient
        )
    }
} 