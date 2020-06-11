from kafka import KafkaProducer
from kafka.errors import KafkaError
import json

producer = KafkaProducer(bootstrap_servers=['localhost:9092'])

with open("daily-fulldata.json", "r") as daily:
    dailyData = json.load(daily)


# produce json messages
producer = KafkaProducer(value_serializer=lambda m: json.dumps(m).encode('ascii'))

for val in dailyData:
    producer.send('json-topic', val)


# block until all async messages are sent
producer.flush()

# configure multiple retries
producer = KafkaProducer(retries=5)