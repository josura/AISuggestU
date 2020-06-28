package controllers

import (
	"fmt"

	"github.com/astaxie/beego"
)

type SuggestionRequest struct {
	Username string `form:"username"`
}

type Suggestion struct {
	Name           string
	ClassifiedRepo ClassifiedRepo
}

type MainController struct {
	beego.Controller
}

func (c *MainController) Get() {
	c.TplName = "index.tpl"
}

func (c *MainController) Post() {
	var s SuggestionRequest

	if err := c.ParseForm(&s); err != nil && s.Username != "" {
		fmt.Println("Request Error")
	}

	fmt.Println("Send starred repos to kafka")
	sendStarredRepo(s.Username)

	fmt.Println("Send suggestions request")
	recommendations := getRecommendation(s.Username)

	fmt.Println(recommendations)
	suggestions := createSuggestionsList(recommendations)

	c.Data["Username"] = s.Username
	c.Data["Suggestions"] = suggestions
	c.TplName = "suggestions.tpl"
}

func createSuggestionsList(recommendations []ClassifiedRepo) []Suggestion {
	var suggestionsList []Suggestion

	for _, val := range recommendations {
		suggestionsList = append(suggestionsList, Suggestion{val.URL[findLastSlashIndex(val.URL)+1:], val})
	}

	return suggestionsList
}

func findLastSlashIndex(val string) int {
	index := 0

	for i, ch := range val {
		if ch == '/' {
			index = i
		}
	}

	return index
}
