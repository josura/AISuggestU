package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"os"

	"gopkg.in/confluentinc/confluent-kafka-go.v1/kafka"
)

type Message struct {
	User     string `json:"user"`
	Url_Repo string `json:"url_repo"`
}

func userStarred(p *kafka.Producer, val []byte) {
	topic := "user-starred-repos"

	p.Produce(&kafka.Message{
		TopicPartition: kafka.TopicPartition{Topic: &topic, Partition: kafka.PartitionAny},
		Value:          val,
	}, nil)

}

func main() {
	argsWithoutProg := os.Args[1:]

	if len(argsWithoutProg) < 1 {
		os.Exit(1)
	}

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

	url := fmt.Sprintf("https://api.github.com/users/%s/starred?per_page=100", argsWithoutProg[0])

	resp, err := http.Get(url)

	if err != nil {
		fmt.Println("ERROR")
	}

	body, err := ioutil.ReadAll(resp.Body)

	if err != nil {
		panic(err)
	}

	var results []map[string]interface{}

	json.Unmarshal([]byte(body), &results)

	for _, result := range results {
		url := fmt.Sprintf("%s", result["url"])
		newMessage := Message{argsWithoutProg[0], url}

		jsonMessage, err := json.Marshal(newMessage)

		if err != nil {
			continue
		}

		userStarred(p, jsonMessage)
	}

}
