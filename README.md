# AISuggestU
<p align="center"><img src="./docs/img/AISuggestLogo.svg" alt="drawing" width="200"/></p>

Project about recommendation systems, primarily GITHUB.
This project was created for a course at UNICT (Technologies for advanced programming).
The technologies used in the main project are Kafka, Spark, Spark streaming, Elastic Search, Kibana, etc...

<p align="center"><img src="./docs/img/AISuggestU_final.png" alt="drawing" width="600"/></p>

# Requirements
- Docker
- Docker Compose

# Usage
First of all clone the project:
```
git clone https://github.com/josura/AISuggestU
```
Then run Zookeeper and Kafka:
```
docker-compose up zoo kafka
```

Run ElasticSearch:
```
docker-compose up elastic-search
```

Run data-ingestion for initial clustering:
```
docker-compose up data-ingestion ...
```

Run Spark Consumer:
```
docker-compose up spark-consumer-classifier
```

Run Web Application:
```
docker-compose up web
```

Run Kibana(optional):
```
docker-compose up kibana
```

# Built with
- [Kafka](https://kafka.apache.org/)
- [Spark](https://spark.apache.org/)
- [ElasticSearch](https://www.elastic.co/)
- [Beego](https://beego.me/)
- [Kibana](https://www.elastic.co/kibana)
