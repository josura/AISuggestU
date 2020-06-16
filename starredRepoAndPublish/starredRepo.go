package main

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"os"

	"gopkg.in/confluentinc/confluent-kafka-go.v1/kafka"
)

//Message rappresents the message to be sent to Kafka
type Message struct {
	User    string `json:"user"`
	URLRepo string `json:"url_repo"`
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

	url := fmt.Sprintf("https://api.github.com/users/%s/starred?per_page=100", argsWithoutProg[0])

	resp, err := http.Get(url)

	if err != nil {
		panic(err)
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
