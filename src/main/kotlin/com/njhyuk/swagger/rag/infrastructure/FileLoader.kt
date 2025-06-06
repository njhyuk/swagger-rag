package com.njhyuk.swagger.rag.infrastructure

import org.springframework.stereotype.Component
import java.io.File

@Component
class FileLoader {
    fun load(path: String): File = File(path)
} 