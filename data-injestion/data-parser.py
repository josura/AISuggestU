import requests
import json
import os
from github import Github
from collections import defaultdict

GITHUB_TOKEN = os.environ['GITHUB_TOKEN']

def cleanLink(value):
    value = value[1:]

    i = 0

    for el in value:
        if el == '>':
            break
        i += 1
    
    return value[:i]

def filterData(data):
    pythonObj = json.loads(data)
    stringDump = "["

    for val in pythonObj:
        repoId, repoUrl, owner =  val['id'], val['url'], val['owner']

        ownerTemp = {}

        ownerTemp['id'] = owner['id']
        ownerTemp['user'] = owner['login']

        stringDump += json.dumps({'id': repoId, 'url': repoUrl, 'owner': ownerTemp}) + ","

    stringDump += ']'
   
    return stringDump



def getRepos(iterationNumber):
    f = open("data.json", "w")
    link = "https://api.github.com/repositories"

    for i in range(0, iterationNumber):
        data = requests.get(link, headers={'Authorization': 'token' +  GITHUB_TOKEN})
        header = data.headers
        filteredData = filterData(data.text)
        f.write(filteredData.encode('utf-8'))
        link = cleanLink(header['link'])

    f.close()

def main():
    getRepos(1)



if __name__ == "__main__":
    main()


