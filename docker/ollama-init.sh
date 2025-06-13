#!/bin/bash

# Ollama 서버를 백그라운드로 실행
ollama serve &

# 서버가 뜰 때까지 대기
until wget -q --spider http://localhost:11434/api/tags; do
    echo "Waiting for Ollama to be ready..."
    sleep 1
done

# 임베딩 모델 다운로드
echo "Pulling nomic-embed-text model..."
ollama pull nomic-embed-text

# 컨테이너 유지
tail -f /dev/null 