package com.njhyuk.swagger.rag.config

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.jackson.*
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class OllamaHttpClientConfig {
    companion object {
        private const val TIMEOUT_SECONDS = 30L
    }

    @Bean
    fun ollamaHttpClient(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            jackson()
        }
        engine {
            config {
                connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
                retryOnConnectionFailure(true)
            }
        }
    }
} 