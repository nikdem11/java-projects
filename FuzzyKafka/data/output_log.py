import json
import csv
import os
from datetime import datetime
from kafka import KafkaConsumer

# Konfiguracja połączenia
TOPIC = 'fuzzy-alerts'
BROKER = 'localhost:9092'
CSV_FILE = 'output_history.csv'

# Inicjalizacja konsumenta
consumer = KafkaConsumer(
    TOPIC,
    bootstrap_servers=[BROKER],
    value_deserializer=lambda x: json.loads(x.decode('utf-8')),
    auto_offset_reset='latest'
)

print(f"[*] Rozpoczęto logowanie wyników do pliku {CSV_FILE}...")
print("[*] Czekam na dane z aplikacji Java...")

try:
    for msg in consumer:
        data = msg.value
        
        # 1. Dodanie timestampu
        row = {'timestamp': datetime.now().strftime("%Y-%m-%d %H:%M:%S")}
        
        # 2. Łączenie z danymi z kafki
        row.update(data)
        print(f"{row['timestamp']} {data.get('product_id')} {data.get('risk')}")

        # 3. Zapis do pliku
        if (data.get('status') ==  'CRITICAL'): 
            file_exists = os.path.isfile(CSV_FILE)
            with open(CSV_FILE, mode='a', newline='') as f:
                writer = csv.DictWriter(f, fieldnames=row.keys(), delimiter=';')
                if not file_exists:
                    writer.writeheader()
                writer.writerow(row)
                print(f"Zapis alertu do pliku")

except KeyboardInterrupt:
    print("\n[*] Logowanie przerwane przez użytkownika.")
