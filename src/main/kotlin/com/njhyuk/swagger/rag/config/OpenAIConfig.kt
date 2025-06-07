package com.njhyuk.swagger.rag.config

import com.aallam.openai.api.logging.LogLevel
import com.aallam.openai.client.LoggingConfig
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAIConfig {
    @Value("\${openai.api.key}")
    private lateinit var apiKey: String

    @Bean
    fun openAI(): OpenAI {
        val config = OpenAIConfig(
            token = apiKey,
            logging = LoggingConfig(logLevel = LogLevel.None),
        )
        return OpenAI(config)
    }
}