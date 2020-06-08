import requests
import json
import os
from github import Github
from collections import defaultdict

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



f = open("data.json", "w")
link = "https://api.github.com/repositories"

for i in range(0, 1):
    data = requests.get(link)
    header = data.headers
    filteredData = filterData(data.text)
    f.write(filteredData.encode('utf-8'))
    link = cleanLink(header['link'])
    #Get url, repoId, owner

# # or using an access token
# g = Github(os.environ['TOKEN_GITHUB'])

# for repo in g.get_user().get_repos():
#     print(repo.name)
#     repo.edit(has_wiki=False)
#     print(dir(repo))
