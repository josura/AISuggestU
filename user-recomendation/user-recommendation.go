package main

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

func main() {
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
	termQuery := elastic.NewTermQuery("owner", "herbrant")
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

	// searchResult is of type SearchResult and returns hits, suggestions,
	// and all kinds of other information from Elasticsearch.
	fmt.Printf("Query took %d milliseconds\n", searchResult.TookInMillis)

	// Each is a convenience function that iterates over hits in a search result.
	// It makes sure you don't need to check for nil values in the response.
	// However, it ignores errors in serialization. If you want full control
	// over iterating the hits, see below.
	// var ttyp ClassifiedRepo
	// for _, item := range searchResult.Each(reflect.TypeOf(ttyp)) {
	// 	if _, ok := item.(ClassifiedRepo); ok {
	// 		fmt.Println("Provola")
	// 	}
	// }
	// TotalHits is another convenience function that works even when something goes wrong.
	fmt.Printf("Found a total of %d classified repos\n", searchResult.TotalHits())

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

		//fmt.Println(t)

		if !contains(labelList, t.Label) {
			labelList = append(labelList, t.Label)
		}
	}

	for _, label := range labelList {
		// Search with a term query
		//labelQuery := elastic.NewTermQuery("label", label)
		ownerNotQuery := elastic.NewBoolQuery().MustNot(elastic.NewTermQuery("owner", "herbrant"))
		//joinedQuery := elastic.NewBoolQuery().Must(labelQuery, ownerNotQuery)

		fmt.Println(label)

		searchResult, err := client.Search().
			Index("repositories"). // search in index "twitter"
			Query(ownerNotQuery).  // specify the query
			From(0).Size(100).     // take documents 0-9
			Pretty(true).          // pretty print request and response JSON
			Do(ctx)                // execute

		if err != nil {
			continue
		}

		var ttyp ClassifiedRepo
		for _, item := range searchResult.Each(reflect.TypeOf(ttyp)) {
			if t, ok := item.(ClassifiedRepo); ok {
				fmt.Println(t)
			}
		}

		fmt.Printf("Found a total of %d classified repos\n", searchResult.TotalHits())
	}
}
