# AISuggestU
<p align="center"><img src="./docs/img/AISuggestLogo.svg" alt="drawing" width="200"/></p>

Project about recommendation systems, primarily GITHUB.
This project was created for a course at UNICT (Technologies for advanced programming).
The technologies used in the main project are Kafka, Spark, Spark streaming, Elastic Search, Kibana, etc...
The presentation of this project is in **docs**

<p align="center"><img src="./docs/img/AISuggestU_final.png" alt="drawing" width="600"/></p>

# Requirements
- Docker
- Docker Compose
- Github token
- Around 5GB of RAM 
- jupyter notebook to see the presentation in **docs**

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

Set GITHUB_TOKEN as an environment variable in your system.

Run data-ingestion for initial clustering:
```
docker-compose up data-ingestion
```
Wait until it finishes.

Run Spark Consumer:
```
docker-compose up spark-consumer-classifier
```
Wait until the first batch arrive (empty batch)

Run Web Application:
```
docker-compose up web
```

Run daily data ingestion (cronjob not set in the first build):
```
docker-compose up daily-data-ingestion
```

Send daily data to kafka:
```
docker-compose up daily-producer
```

Run Kibana(optional):
```
docker-compose up kibana
```

## Data Visualization
To see the data in kibana go to **localhost:5601** and specify the index **repositories** 

## Web application 
To access the web application go to **localhost:8080**

# Built with
- [Kafka](https://kafka.apache.org/)
- [Spark](https://spark.apache.org/)
- [ElasticSearch](https://www.elastic.co/)
- [Beego](https://beego.me/)
- [Kibana](https://www.elastic.co/kibana)
