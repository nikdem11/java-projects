import pandas as pd
import json
import time
import random
from kafka import KafkaProducer

csv_path = 'ai4i2020.csv'
df = pd.read_csv(csv_path)
 
# Konfiguracja producenta
producer = KafkaProducer(
    bootstrap_servers=['localhost:9092'],
    value_serializer=lambda v: json.dumps(v).encode('utf-8')
)

print("Symulator wystartował. Wysyłam dane co 2 sekundy...")
i = 0

try:
    while True:
        row = df.iloc[i]

        # Przygotowanie paczki danych (5 atrybutów)
        payload = {
	    "product_id": str(row['Product ID']),	
            "air_temp": float(row['Air temperature [K]']),
            "process_temp": float(row['Process temperature [K]']),
            "rot_speed": float(row['Rotational speed [rpm]']),
            "torque": float(row['Torque [Nm]']),
            "tool_wear": float(row['Tool wear [min]'])	
        }

        # Wyslanie do kafki
        producer.send('industrial-raw-data', value=payload)
        print(f"Wysłano rekord: {payload}")
        i += 1
        time.sleep(2)
except KeyboardInterrupt:
    print("Symulator zatrzymany.")
