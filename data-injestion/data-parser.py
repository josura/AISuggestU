import requests
import json
import os
import base64
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

    stringDump = stringDump[:-1]
    stringDump += ']'
   
    return stringDump



def getRepos(iterationNumber):
    f = open("data.json", "w")
    link = "https://api.github.com/repositories"

    for i in range(0, iterationNumber):
        data = requests.get(link, headers={'Authorization': 'token ' +  GITHUB_TOKEN})
        header = data.headers
        filteredData = filterData(data.text)
        f.write(filteredData.encode('utf-8'))
        link = cleanLink(header['link'])

    f.close()

def getReadme():
    with open('data.json') as json_file:
        data = json.load(json_file)

    for repo in data:
        repoUrl = repo['url']

        response = requests.get(repoUrl + "/readme", headers={'Authorization': 'token ' +  GITHUB_TOKEN})

        if response.status_code != 200:
            continue
        
        readme = json.loads(response.text)['content']
        repo['readme'] = base64.b64decode(readme)
    
    dumpedData = json.dumps(data)

    with open('fulldata.json', 'w') as json_file:
        json_file.write(dumpedData)
        



def main():
    getRepos(1)
    getReadme()



if __name__ == "__main__":
    main()


