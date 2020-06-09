import requests
import json
import os
import sys
import logging
import base64
from github import Github
from collections import defaultdict
#removing stopwords
import nltk
from nltk.corpus import stopwords 
from nltk.tokenize import word_tokenize

GITHUB_TOKEN = os.environ['GITHUB_TOKEN']
logfile='data-parser.log'
logging.basicConfig(filename=logfile, format = '%(asctime)s  %(levelname)-10s %(processName)s  %(name)s %(message)s', level = logging.INFO)


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

def getReadmeUrl(repoUrl):
    return repoUrl + "/readme"


def getRepos(iterationNumber):
    f = open("data.json", "w")
    link = "https://api.github.com/repositories"

    for _ in range(0, iterationNumber):
        data = requests.get(link, headers={'Authorization': 'token ' +  GITHUB_TOKEN})
        header = data.headers
        filteredData = filterData(data.text)
        f.write(filteredData.encode('utf-8'))
        link = cleanLink(header['link'])

    f.close()
    logging.info("Repos written to data.json.")

def removeStopWords(stringToClean,stop_words):
    word_tokens = word_tokenize(stringToClean.decode('utf-8')) 
    filtered_sentence = [w for w in word_tokens if not w in stop_words] 
    return ' '.join(e.encode('utf-8') for e in filtered_sentence)



def getReadme():
    with open('data.json') as json_file:
        data = json.load(json_file)

    for repo in data:
        repoUrl = repo['url']
        readmeUrl = getReadmeUrl(repoUrl)

        response = requests.get(readmeUrl, headers={'Authorization': 'token ' +  GITHUB_TOKEN})

        if response.status_code != 200:
            logging.warning("Response code isn't 200 {}".format(readmeUrl))
            continue
        
        readmedecoded = base64.b64decode(json.loads(response.text)['content'])
        stop_words = set(stopwords.words('english'))
        stop_words.add(',')
        stop_words.add('.')
        stop_words.add(':')
        stop_words.add('#')
        stop_words.add('?')
        repo['readme'] = removeStopWords(readmedecoded,stop_words)
    
    dumpedData = json.dumps(data)

    with open('fulldata.json', 'w') as json_file:
        json_file.write(dumpedData)
        logging.info("Repos Readme written to fulldata.json.")
        



def main():
    nltk.download('stopwords')
    nltk.download('punkt')
    if len(sys.argv) > 1:
        iterationNumber = int(sys.argv[1])
        getRepos(iterationNumber)
        getReadme()
    else:
        print("Use: python {} number_of_page".format(sys.argv[0]))



if __name__ == "__main__":
    main()


