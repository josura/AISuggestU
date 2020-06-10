import json
import os, sys

from elasticsearch import Elasticsearch, RequestsHttpConnection

if sys.argv > 1:
    ES_CLUSTER = 'http://127.0.0.1:9200/' # Need PW and User name
    ES_INDEX = 'daily-data'
    ES_TYPE = 'json'

    es = Elasticsearch(
        hosts=[{'host': '127.0.0.1', 'port': 9200}]
    )
    
    with open(sys.argv[1]) as json_file:
        repos = json.load(json_file)

        for repo in repos:
            res = es.index(index='repository', id=repo['url'], body=repo)
