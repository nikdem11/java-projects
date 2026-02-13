#!/bin/bash

KAFKA_HOME="/home/student/coitbd-project/kafka_2.13-3.9.0"

echo "[System] Uruchamianie serwera Kafka..."
$KAFKA_HOME/bin/kafka-server-start.sh $KAFKA_HOME/config/kraft/server.properties
