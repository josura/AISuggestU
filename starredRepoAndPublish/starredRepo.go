package main

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"os"

	"gopkg.in/confluentinc/confluent-kafka-go.v1/kafka"
)

type Message struct {
	User    string `json:"user"`
	URLRepo string `json:"url_repo"`
	Readme  string `json:"readme"`
}

type ReadmeResponse struct {
	Name        string `json:"name"`
	Path        string `json:"path"`
	Sha         string `json:"sha"`
	Size        int    `json:"size"`
	URL         string `json:"url"`
	HTMLURL     string `json:"html_url"`
	GitURL      string `json:"git_url"`
	DownloadURL string `json:"download_url"`
	Type        string `json:"type"`
	Content     string `json:"content"`
	Encoding    string `json:"encoding"`
	Links       struct {
		Self string `json:"self"`
		Git  string `json:"git"`
		HTML string `json:"html"`
	} `json:"_links"`
}

func initKafkaProducer() *kafka.Producer {
	p, err := kafka.NewProducer(&kafka.ConfigMap{"bootstrap.servers": "kafka:9092"})

	if err != nil {
		panic(err)
	}

	return p
}

func userStarred(p *kafka.Producer, val []byte) {
	topic := "user-starred-repos"

	p.Produce(&kafka.Message{
		TopicPartition: kafka.TopicPartition{Topic: &topic, Partition: kafka.PartitionAny},
		Value:          val,
	}, nil)

}

func getGithubTokenHeader() string {
	return fmt.Sprintf("token %s", os.Getenv("GITHUB_TOKEN"))
}

func base64ToString(value string) (string, error) {
	converted, err := base64.StdEncoding.DecodeString(value)
	return string(converted), err
}

func getReadme(url string) (string, error) {
	readmeURL := url + "/readme"

	client := &http.Client{}
	req, _ := http.NewRequest("GET", readmeURL, nil)
	req.Header.Set("Authorization", getGithubTokenHeader())
	resp, err := client.Do(req)

	if err != nil {
		fmt.Println("Error")
	}

	defer resp.Body.Close()
	body, err := ioutil.ReadAll(resp.Body)

	var bodyStruct ReadmeResponse
	err = json.Unmarshal([]byte(body), &bodyStruct)

	if err != nil {
		fmt.Println(err)
	}

	if err != nil {
		fmt.Println(err)
	}

	readme, err := base64ToString(bodyStruct.Content)

	return readme, err
}

func main() {
	argsWithoutProg := os.Args[1:]

	if len(argsWithoutProg) < 1 {
		fmt.Println("Use: " + os.Args[0] + " USER")
		os.Exit(1)
	}

	url := fmt.Sprintf("https://api.github.com/users/%s/starred?per_page=100", argsWithoutProg[0])

	client := &http.Client{}
	req, _ := http.NewRequest("GET", url, nil)

	fmt.Println(getGithubTokenHeader())
	req.Header.Set("Authorization", getGithubTokenHeader())

	resp, err := client.Do(req)

	if err != nil {
		panic(err)
	}

	body, err := ioutil.ReadAll(resp.Body)

	if err != nil {
		panic(err)
	}

	var results []map[string]interface{}

	json.Unmarshal([]byte(body), &results)

	p := initKafkaProducer()

	for _, result := range results {
		url := fmt.Sprintf("%s", result["url"])
		readme, err := getReadme(url)

		if err != nil {
			continue
		}

		newMessage := Message{argsWithoutProg[0], url, readme}

		jsonMessage, err := json.Marshal(newMessage)

		if err != nil {
			continue
		}

		fmt.Println(string(jsonMessage))
		userStarred(p, jsonMessage)
	}
}
