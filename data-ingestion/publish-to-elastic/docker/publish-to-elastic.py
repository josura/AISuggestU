import json
import os, sys

from elasticsearch import Elasticsearch

if sys.argv > 1:
    ES_CLUSTER = 'http://localhost:9200/' # Need PW and User name
    ES_INDEX = 'test'
    ES_TYPE = 'doc'

    es = Elasticsearch(
        ['localhost'],
        http_auth=('elastic', 'changeme'),
        port=9200
    )
    es = Elasticsearch(ES_CLUSTER)
    with open(sys.argv[1]) as json_file:
        json_docs = json.load(json_file)
    es.bulk(ES_INDEX, ES_TYPE, json_docs)