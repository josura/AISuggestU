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
    dataDict = data.loads()
    
    # regroup data
    d = defaultdict(dict)
    for item in dataDict:
        d[item["id"]].update(item)



f = open("data.json", "w")
link = "https://api.github.com/repositories"

for i in range(0, 3):
    data = requests.get(link)
    header = data.headers
    filterData(data.json)
    f.write(data.text.encode('utf-8'))
    link = cleanLink(header['link'])
    #Get url, repo_id, owner

# # or using an access token
# g = Github(os.environ['TOKEN_GITHUB'])

# for repo in g.get_user().get_repos():
#     print(repo.name)
#     repo.edit(has_wiki=False)
#     print(dir(repo))
