#!/bin/bash

# Ścieżka do Kafki
KAFKA_HOME="/home/student/coitbd-project/kafka_2.13-3.9.0"

echo "[System] Czyszczenie starych danych..."
rm -rf /tmp/kafka-logs /tmp/kraft-combined-logs /tmp/kafka-streams

echo "[System] Generowanie nowego ID klastra..."
K_ID=$($KAFKA_HOME/bin/kafka-storage.sh random-uuid)

echo "[System] Formatowanie magazynu KRaft (ID: $K_ID)..."
$KAFKA_HOME/bin/kafka-storage.sh format -t $K_ID -c $KAFKA_HOME/config/kraft/server.properties

echo "[System] Gotowe. Możesz teraz uruchomić serwer."
