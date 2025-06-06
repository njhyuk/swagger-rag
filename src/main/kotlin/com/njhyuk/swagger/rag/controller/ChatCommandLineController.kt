package com.njhyuk.swagger.rag.controller

import com.njhyuk.swagger.rag.usecase.QueryAnswerUseCase
import com.njhyuk.swagger.rag.usecase.ParseSwaggerUseCase
import com.njhyuk.swagger.rag.infrastructure.FileLoader
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class ChatCommandLineController(
    private val queryAnswerUseCase: QueryAnswerUseCase,
    private val parseSwaggerUseCase: ParseSwaggerUseCase,
    private val fileLoader: FileLoader
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        // Swagger 파일 로드 및 파싱
        val file = fileLoader.load("openapi/sample-api.json")
        val chunks = parseSwaggerUseCase.parse(file)
        queryAnswerUseCase.loadChunks(chunks)

        println("Swagger 파일을 불러왔습니다. 질문을 입력하세요.")
        while (true) {
            print("> ")
            val input = readLine() ?: break
            if (input.lowercase() == "exit") break
            val answer = queryAnswerUseCase.answer(input)
            println(answer)
        }
    }
} 