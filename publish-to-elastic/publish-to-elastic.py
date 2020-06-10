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
    
    es = Elasticsearch(ES_CLUSTER)
    with open(sys.argv[1]) as json_file:
        repos = json.load(json_file)

        for repo in repos:
            es.bulk(ES_INDEX, ES_TYPE, )

    # e1={
    # "first_name" :  "asd",
    # "last_name" :   "asdasd",
    # "age" :         33,
    # "about" :       "I like to collect rock albums",
    # "interests":  [ "music" ]
    # }
    # res = es.index(index='pippo2',id=100,body=e1)
