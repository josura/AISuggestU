package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"os"

	"gopkg.in/confluentinc/confluent-kafka-go.v1/kafka"
)

//Repositories rappresents all daily repos
type Repositories []struct {
	URL    string `json:"url"`
	Owner  string `json:"owner"`
	Readme string `json:"readme"`
	Stars  int    `json:"stars"`
}

func loadDailyRepos() Repositories {
	dailyReposFile, err := os.Open("/data/daily-fulldata.json")

	if err != nil {
		fmt.Println(err)
	}

	byteValue, _ := ioutil.ReadAll(dailyReposFile)
	var repos Repositories
	json.Unmarshal(byteValue, &repos)

	return repos
}

func main() {

	p, err := kafka.NewProducer(&kafka.ConfigMap{"bootstrap.servers": "kafka:9092"})
	if err != nil {
		panic(err)
	}

	defer p.Close()

	// Delivery report handler for produced messages
	go func() {
		for e := range p.Events() {
			switch ev := e.(type) {
			case *kafka.Message:
				if ev.TopicPartition.Error != nil {
					fmt.Printf("Delivery failed: %v\n", ev.TopicPartition)
				} else {
					fmt.Printf("Delivered message to %v\n", ev.TopicPartition)
				}
			}
		}
	}()

	//Load json data
	repos := loadDailyRepos()

	// Produce messages to topic (asynchronously)
	topic := "daily-repos"

	for _, repo := range repos {
		val, err := json.Marshal(repo)

		if err != nil {
			continue
		}

		p.Produce(&kafka.Message{
			TopicPartition: kafka.TopicPartition{Topic: &topic, Partition: kafka.PartitionAny},
			Value:          val,
		}, nil)
	}

	// Wait for message deliveries before shutting down
	p.Flush(15 * 1000)
}
