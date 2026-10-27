#!/bin/bash

BASE_URL="http://product-api:8080"
HEALTH_ENDPOINT="$BASE_URL/actuator/health"

echo "Esperando a que la API esté lista en $HEALTH_ENDPOINT..."

# Espera activa (timeout 60s)
MAX_RETRIES=12
RETRY_COUNT=0
until curl -s "$HEALTH_ENDPOINT" | grep UP > /dev/null; do
  RETRY_COUNT=$((RETRY_COUNT+1))
  if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
    echo "Error: API no disponible después de 60 segundos."
    exit 1
  fi
  echo "Esperando API ($RETRY_COUNT/$MAX_RETRIES)..."
  sleep 5
done

echo "API lista. Iniciando benchmark con k6..."

# Run k6
k6 run /app/benchmark.js

echo "Benchmark completado!"
