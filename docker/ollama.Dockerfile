FROM ollama/ollama:latest

USER root
RUN apt-get update && apt-get install -y wget

ENTRYPOINT ["/bin/bash", "/ollama-init.sh"] 