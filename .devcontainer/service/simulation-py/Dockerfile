FROM python:3.9-slim

WORKDIR /app

RUN pip install paho-mqtt

COPY main.py .

CMD ["python", "main.py"]