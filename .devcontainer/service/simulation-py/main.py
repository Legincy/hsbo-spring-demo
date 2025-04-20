import paho.mqtt.client as mqtt
import json
import time
import random
from datetime import datetime
import os

MQTT_BROKER = "mosquitto"
MQTT_PORT = 1883
MQTT_TOPIC_BASE = "S7_1500/Temperatur"
MQTT_USERNAME = os.getenv("MQTT_USERNAME", "mqtt_user")
MQTT_PASSWORD = os.getenv("MQTT_PASSWORD", "mqtt_password")

SOLL_TEMP = 22.0
ist_temp = 21.0
differenz_temp = SOLL_TEMP - ist_temp

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print(f"Successfully connected to MQTT Broker {MQTT_BROKER}:{MQTT_PORT}")
    else:
        print(f"Error while connecting to MQTT Broker: {rc}")

def on_disconnect(client, userdata, rc):
    print(f"Disconnected from MQTT Broker {MQTT_BROKER}:{MQTT_PORT} with result code {rc}")

client = mqtt.Client()
client.on_connect = on_connect
client.on_disconnect = on_disconnect

client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)

try:
    client.connect(MQTT_BROKER, MQTT_PORT, 60)
    client.loop_start()
except Exception as e:
    print(f"Error connecting to MQTT Broker: {e}")
    exit(1)

soll_temp_sent = False
try:
    while True:
        ist_temp = ist_temp + (SOLL_TEMP - ist_temp) * 0.1 + random.uniform(-0.8, 0.8)
        differenz_temp = SOLL_TEMP - ist_temp

        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        '''
        data_soll = {
            "value": SOLL_TEMP,
            "unit": "°C",
            "timestamp": timestamp
        }

        data_ist = {
            "value": round(ist_temp, 2),
            "unit": "°C",
            "timestamp": timestamp
        }

        data_diff = {
            "value": round(differenz_temp, 2),
            "unit": "°C",
            "timestamp": timestamp
        }
        '''

        data_soll = SOLL_TEMP
        data_ist = round(ist_temp, 2)
        data_diff = round(differenz_temp, 2)

        '''
        client.publish(f"{MQTT_TOPIC_BASE}/Soll", json.dumps(data_soll))
        client.publish(f"{MQTT_TOPIC_BASE}/Ist", json.dumps(data_ist))
        client.publish(f"{MQTT_TOPIC_BASE}/Differenz", json.dumps(data_diff))
        '''

        if(soll_temp_sent == False):
            client.publish(f"{MQTT_TOPIC_BASE}/Soll", json.dumps(data_soll))
            soll_temp_sent = True

        client.publish(f"{MQTT_TOPIC_BASE}/Ist", data_ist)
        client.publish(f"{MQTT_TOPIC_BASE}/Differenz", data_diff)

        print(f"Sending data to MQTT Broker: Soll: {data_soll} °C, Ist: {data_ist} °C, Differenz: {data_diff} °C")

        time.sleep(2)

except KeyboardInterrupt:
    client.loop_stop()
    client.disconnect()