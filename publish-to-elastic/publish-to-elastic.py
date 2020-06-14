import json
import os, sys

from elasticsearch import Elasticsearch, RequestsHttpConnection

if sys.argv > 1:
    es = Elasticsearch(
        hosts=[{'host': 'elastic-search', 'port': 9200}]
    )
    
    with open(sys.argv[1]) as json_file:
        repos = json.load(json_file)

        for repo in repos:
            res = es.index(index='repository', id=repo['url'], body=repo)
