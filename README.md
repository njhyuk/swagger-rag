# swagger-rag

Spring Boot + Kotlin 기반의 오픈소스 프로젝트로, Swagger(OpenAPI) JSON 파일을 파싱하여 API 문서를 자연어로 분할/임베딩하고, CLI 챗봇 형태로 질의응답이 가능한 RAG(Retrieval-Augmented Generation) 시스템입니다.

## 주요 기능
- Swagger(OpenAPI) JSON 파일 파싱 및 API 설명 추출
- 각 API를 자연어 문서 chunk로 변환 (임베딩/검색에 활용)
- CLI 챗봇 인터페이스 제공 (MVP)
- 향후 SlackBot 등 다양한 인터페이스로 확장 가능

## 디렉토리 구조
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
│   │   │   │   └── QueryAnswerUseCase.kt
│   │   │   ├── domain/
│   │   │   │   └── model/
│   │   │   │       ├── ApiDocChunk.kt
│   │   │   │       └── ChatQuery.kt
│   │   │   ├── adapter/
│   │   │   │   ├── input/cli/
│   │   │   │   └── output/embedding/
│   │   │   └── infrastructure/
│   │   │       └── FileLoader.kt
│   └── test/
│       └── kotlin/com/njhyuk/swagger/rag/
│           └── SampleParseTest.kt
└── src/main/resources/
    └── application.yml
```

## 실행 방법
```bash
./gradlew bootRun
```

## 예시 입력/출력
```
> Swagger 파일을 불러왔습니다. 질문을 입력하세요.
> GET /users API는 어떤 역할인가요?
- [GET /users] 사용자 목록을 조회합니다. ...
```

## 테스트 실행
```bash
./gradlew test
```

---

## License
MIT 