# swagger-rag

A Kotlin + Spring Boot open-source project that parses Swagger (OpenAPI) JSON files, splits and embeds API documentation as natural language chunks, and enables Retrieval-Augmented Generation (RAG) Q&A via a CLI chatbot. Easily extensible to other interfaces (e.g., SlackBot).

## Features
- Parse Swagger (OpenAPI) JSON files and extract API descriptions
- Convert each API into natural language document chunks (for embedding/search)
- Provide a CLI chatbot interface (MVP)
- Extensible to other interfaces (e.g., SlackBot) in the future

## Directory Structure
```
swagger-rag/
├── build.gradle.kts
├── settings.gradle.kts
├── README.md
├── openapi/
│   └── sample-api.json
├── src/
│   ├── main/
│   │   ├── kotlin/com/njhyuk/swagger/rag/
│   │   │   ├── Application.kt
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   │   └── ChatCommandLineController.kt
│   │   │   ├── usecase/
│   │   │   │   ├── ParseSwaggerUseCase.kt
│   │   │   │   ├── GenerateChunkUseCase.kt
│   │   │   │   ├── QueryAnswerUseCase.kt
│   │   │   │   └── ChunkRepository.kt
│   │   │   ├── domain/
│   │   │   │   └── model/
│   │   │   │       ├── ApiDocChunk.kt
│   │   │   │       └── ChatQuery.kt
│   │   │   ├── adapter/
│   │   │   │   ├── input/cli/
│   │   │   │   └── output/embedding/
│   │   │   │       └── OpenAIEmbeddingClient.kt
│   │   │   └── infrastructure/
│   │   │       └── FileLoader.kt
│   └── test/
│       └── kotlin/com/njhyuk/swagger/rag/
│           ├── usecase/
│           │   ├── GenerateChunkUseCaseTest.kt
│           │   ├── ChunkRepositoryTest.kt
│           │   ├── TextMatchScoreCalculatorTest.kt
│           │   └── CosineSimilarityCalculatorTest.kt
│           └── domain/
└── src/main/resources/
    └── application.yml
```

## Requirements
- JDK 21 or higher
- Gradle (wrapper included)
- OpenAI API key (for embedding, set `OPENAI_API_KEY` environment variable)

## Getting Started

1. **Clone the repository:**
```bash
git clone https://github.com/your-username/swagger-rag.git
cd swagger-rag
```

2. **Place your OpenAPI JSON file:**
   - Put your Swagger/OpenAPI JSON file in the `openapi/` directory (e.g., `openapi/sample-api.json`).

3. **Set your OpenAI API key:**
```bash
export OPENAI_API_KEY=your_openai_api_key
```

4. **Run the application:**
```bash
./gradlew bootRun
```

## Example CLI Usage
```
> Swagger file loaded. Please enter your question.
> What does the GET /users API do?
- [GET /users] Returns the list of users. ...
```

## Running Tests
```bash
./gradlew test
```

## Dependencies
- Spring Boot 3.2+
- Kotlin 1.9+
- OpenAI Java Client
- Ktor HTTP Client
- H2 Database (for development/testing)
- JUnit 5 (for testing)

## Contributing
Contributions are welcome! Please open issues or submit pull requests for bug fixes, new features, or improvements. For major changes, please open an issue first to discuss what you would like to change.

## License
MIT 