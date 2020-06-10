import requests
import sys

URL = "http://127.0.0.1:9200/repository/_search"


if sys.argv > 1:
    body = """{
        "query" : {
            "term" : { "owner" : "%s" }
        }
    }""" % str(sys.argv[1])
    
    headers = {"Content-type": "application/json", "Accept":"text/plain"}

    response = requests.post(url = URL, data = body, headers = headers)

    if response.status_code == 200:
        print(response.text)
    else:
        print("Error code {}".format(response.status_code))
else:
    print("Use {} owner".format(sys.argv[0]))