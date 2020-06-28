package controllers

import (
	"encoding/base64"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
	"regexp"
	"strings"

	"github.com/bbalet/stopwords"
	"gopkg.in/confluentinc/confluent-kafka-go.v1/kafka"
)

type Message struct {
	Owner  string `json:"owner"`
	URL    string `json:"url"`
	Readme string `json:"readme"`
	Stars  int    `json:"stars"`
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
	p, err := kafka.NewProducer(&kafka.ConfigMap{"bootstrap.servers": "127.0.0.1:29092"})

	if err != nil {
		fmt.Println("Error initKafkaProducer")
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

func cleanReadme(text *string) {
	reg := regexp.MustCompile(`[^a-zA-Z0-9]+`)
	*text = reg.ReplaceAllString(*text, " ")
	reg = regexp.MustCompile(`[0-9]+`)
	*text = reg.ReplaceAllString(*text, " ")

	for i := 0; i < 5; i++ {
		reg = regexp.MustCompile(`[ ]+[a-z|A-Z][ ]+`)
		*text = reg.ReplaceAllString(*text, " ")
	}

	*text = stopwords.CleanString(*text, "en", true)
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
	cleanReadme(&readme)

	return readme, err
}

func clearURL(url string) string {
	return strings.Replace(url, "https://api.github.com/repos", "https://github.com", 1)
}

func sendStarredRepo(user string) {

	url := fmt.Sprintf("https://api.github.com/users/%s/starred?per_page=100", user)

	client := &http.Client{}
	req, _ := http.NewRequest("GET", url, nil)
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
		url, _ := result["url"].(string)
		readme, err := getReadme(url)

		if err != nil {
			continue
		}

		stars := int(result["stargazers_count"].(float64))

		newMessage := Message{user, clearURL(url), readme, stars}

		jsonMessage, err := json.Marshal(newMessage)

		if err != nil {
			continue
		}

		userStarred(p, jsonMessage)
	}
}
