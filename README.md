# swagger-rag

A Kotlin + Spring Boot open-source project that parses Swagger (OpenAPI) JSON files, splits and embeds API documentation as natural language chunks, and enables Retrieval-Augmented Generation (RAG) Q&A via a CLI chatbot. Easily extensible to other interfaces (e.g., SlackBot).

## Features
- Parse Swagger (OpenAPI) JSON files and extract API descriptions
- Convert each API into natural language document chunks (for embedding/search)
- Provide a CLI chatbot interface (MVP)
- Extensible to other interfaces (e.g., SlackBot) in the future
- Vector similarity search for accurate API documentation retrieval
- Natural language processing for better query understanding

## Usage Demo

```bash
> How do I create a new user?
Found 3 relevant APIs:

1. [POST] /users
   Register New User
Registers a new user in the system.

**Responses:**
[201] User successfully created
   Relevance score: 0.81

2. [POST] /orders
   Create New Order
Creates a new order.

**Responses:**
[201] Order successfully created
   Relevance score: 0.72

3. [POST] /products
   Register New Product
Registers a new product in the system.

**Responses:**
[201] Product successfully created
   Relevance score: 0.72
```

## Requirements
- JDK 21 or higher
- Gradle (wrapper included)
- OpenAI API key (for embedding, set `OPENAI_API_KEY` environment variable)
- Docker (for Qdrant Vector DB)

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

4. **Start Qdrant Vector DB:**
```bash
docker-compose up -d
```

5. **Run the application:**
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
- Spring Boot 3.2.5
- Kotlin 1.9.23
- OpenAI Java Client 4.0.1
- Ktor HTTP Client 3.0.0

## Contributing
Contributions are welcome! Please open issues or submit pull requests for bug fixes, new features, or improvements. For major changes, please open an issue first to discuss what you would like to change.

## License
MIT 