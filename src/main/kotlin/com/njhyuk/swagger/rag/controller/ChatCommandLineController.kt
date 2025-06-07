package com.njhyuk.swagger.rag.controller

import com.njhyuk.swagger.rag.service.ChatService
import com.njhyuk.swagger.rag.service.SwaggerLoader
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class ChatCommandLineController(
    private val chatService: ChatService,
    private val swaggerLoader: SwaggerLoader
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        // Swagger 파일들 로드 및 초기화
        val chunks = swaggerLoader.loadAll()
        chatService.initialize(chunks)

        println("Swagger Files Loaded. Enter your question.")
        
        while (true) {
            print("> ")
            val input = readLine() ?: break
            if (input.lowercase() == "exit") break
            val answer = chatService.processQuestion(input)
            println(answer)
        }
    }
} 