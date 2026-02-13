import json
import matplotlib.pyplot as plt
from kafka import KafkaConsumer
from matplotlib.animation import FuncAnimation

# Konfiguracja
TOPIC = 'fuzzy-alerts'
BROKER = 'localhost:9092'

y_data = []

# Inicjalizacja konsumenta Kafki
consumer = KafkaConsumer(
    TOPIC,
    bootstrap_servers=[BROKER],
    value_deserializer=lambda x: json.loads(x.decode('utf-8')),
    auto_offset_reset='latest'
)

plt.style.use('dark_background')
fig, ax = plt.subplots()
line, = ax.plot([], [], lw=2, color='#00ff00')
ax.set_ylim(0, 100)
ax.set_xlim(-50, 10)
ax.set_title("Monitor Ryzyka Awarii")
ax.set_xlabel("Ostatnie odczyty")
ax.set_ylabel("Ryzyko")

plt.axvline(x=0, color='white', linestyle='--', alpha=0.3)

def init():
    line.set_data([], [])
    return line,

def update(frame):
    # Pobranie wiadomosci z kafki
    msg_pack = consumer.poll(timeout_ms=100)
    
    for tp, messages in msg_pack.items():
        for msg in messages:
            risk = msg.value.get('risk', 0)
            y_data.append(risk)
            
            # Ostatnie 50 rekordow na wykresie
            if len(y_data) > 50:
                y_data.pop(0)

    current_x = range(-len(y_data) + 1, 1)	
    line.set_data(current_x, y_data)

    line.set_color('#ff0000' if (y_data and y_data[-1] > 70) else '#00ff00')

    return line,

ani = FuncAnimation(fig, update, init_func=init, blit=True, interval=200, cache_frame_data=False)
plt.show()
