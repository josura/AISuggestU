package controllers

import (
	"context"
	"encoding/json"
	"fmt"
	"reflect"

	elastic "github.com/olivere/elastic/v7"
)

// ClassifiedRepo is a structure used for serializing/deserializing data in Elasticsearch.
type ClassifiedRepo struct {
	URL   string `json:"url"`
	Owner string `json:"owner"`
	Label int    `json:"label"`
}

func contains(s []int, e int) bool {
	for _, a := range s {
		if a == e {
			return true
		}
	}
	return false
}

func getRecommendation(user string) []ClassifiedRepo {
	ctx := context.Background()

	// Obtain a client and connect to the default Elasticsearch installation
	// on 127.0.0.1:9200. Of course you can configure your client to connect
	// to other hosts and configure it in various other ways.
	client, err := elastic.NewClient()
	if err != nil {
		// Handle error
		panic(err)
	}

	// Ping the Elasticsearch server to get e.g. the version number
	info, code, err := client.Ping("http://127.0.0.1:9200").Do(ctx)
	if err != nil {
		// Handle error
		panic(err)
	}
	fmt.Printf("Elasticsearch returned with code %d and version %s\n", code, info.Version.Number)

	// Getting the ES version number is quite common, so there's a shortcut
	esversion, err := client.ElasticsearchVersion("http://127.0.0.1:9200")
	if err != nil {
		// Handle error
		panic(err)
	}
	fmt.Printf("Elasticsearch version %s\n", esversion)

	// Search with a term query
	termQuery := elastic.NewMatchQuery("owner", user)
	searchResult, err := client.Search().
		Index("repositories"). // search in index "twitter"
		Query(termQuery).      // specify the query
		From(0).Size(1000).    // take documents 0-9
		Pretty(true).          // pretty print request and response JSON
		Do(ctx)                // execute
	if err != nil {
		// Handle error
		panic(err)
	}

	var labelList []int

	// Iterate through results
	for _, hit := range searchResult.Hits.Hits {
		// hit.Index contains the name of the index

		// Deserialize hit.Source into a Tweet (could also be just a map[string]interface{}).
		var t ClassifiedRepo
		err := json.Unmarshal(hit.Source, &t)

		if err != nil {
			continue
		}

		if !contains(labelList, t.Label) {
			labelList = append(labelList, t.Label)
		}
	}

	var ClassifiedRepoList []ClassifiedRepo

	for _, label := range labelList {
		// Search with a term query
		labelQuery := elastic.NewTermQuery("label", label)
		ownerNotQuery := elastic.NewBoolQuery().MustNot(elastic.NewMatchQuery("owner", user))
		joinedQuery := elastic.NewBoolQuery().Must(labelQuery, ownerNotQuery)

		searchResult, err := client.Search().
			Index("repositories"). // search in index "twitter"
			Query(joinedQuery).    // specify the query
			From(0).Size(100).     // take documents 0-9
			Sort("stars", false).
			Size(5).
			Pretty(true). // pretty print request and response JSON
			Do(ctx)       // execute

		if err != nil {
			continue
		}

		var ttyp ClassifiedRepo
		for _, item := range searchResult.Each(reflect.TypeOf(ttyp)) {
			if t, ok := item.(ClassifiedRepo); ok {
				ClassifiedRepoList = append(ClassifiedRepoList, t)
			}
		}
	}

	return ClassifiedRepoList
}
