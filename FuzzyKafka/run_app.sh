#!/bin/bash

# Wejscie do folderu projektu
cd ~/coitbd-project/fuzzy-streams-app

echo "[System] Budowanie aplikacji..."
mvn clean compile

echo "[System] Uruchamianie aplikacji..."
mvn exec:java -Dexec.mainClass="pl.fuzzy.industrial.App"
