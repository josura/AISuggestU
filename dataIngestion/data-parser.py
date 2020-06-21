import requests
import json
import os
import sys
import logging
import base64
import re
from github import Github
from collections import defaultdict
#removing stopwords
import nltk
from nltk.corpus import stopwords 
from nltk.tokenize import word_tokenize

GITHUB_TOKEN = os.environ['GITHUB_TOKEN']
DATAPATH = "/data/"
logfile='/dev/stdout'
logging.basicConfig(filename=logfile, format = '%(asctime)s  %(levelname)-10s %(processName)s  %(name)s %(message)s', level = logging.INFO)

def checkUrlOrApi(link):
    return "https://api" in link

def removeStopWords(stringToClean,stop_words):
    word_tokens = word_tokenize(str(stringToClean)) 
    filtered_sentence = [w for w in word_tokens if not w in stop_words] 
    return ' '.join(e for e in filtered_sentence)

def cleanReadme(readme):
    readme = readme.strip().replace('\\n', '')
    readme = re.sub("[^a-zA-Z0-9]+", ' ', readme)
    readme = re.sub('[0-9]+', " ", readme)
    for _ in range(0, 5):
        readme = re.sub('[ ]+[a-z|A-Z][ ]+', " ", readme)
    stop_words = set(stopwords.words('english'))

    return removeStopWords(readme.lower(),stop_words)

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

    stringDump = ""

    for val in pythonObj:
        repoUrl, owner = val['url'], val['owner']['login']

        stringDump += json.dumps({'url': repoUrl, 'owner': owner}) + ","

    stringDump = stringDump[:-1]

    counter = 0

    for val in stringDump:
        if val == ']':
            counter += 1
    
    print("Counter {}".format(counter))
   
    return stringDump

def filterDailyData(data):
    pythonObj = json.loads(data)
    stringDump = "["

    for val in pythonObj:
        repoUrl, owner, stars = val['url'], val['author'], val['stars']

        stringDump += json.dumps({'url': repoUrl, 'owner': owner, 'stars': stars}) + ","

    stringDump = stringDump[:-1]
    stringDump += ']'
   
    return stringDump


def getRepos(iterationNumber):
    f = open(DATAPATH + "data.json", "w")
    link = "https://api.github.com/repositories"

    f.write("[")

    for i in range(0, iterationNumber):
        data = requests.get(link, headers={'Authorization': 'token ' +  GITHUB_TOKEN})

        if data.status_code != 200:
            logging.warning("Status code != 200 repo " + link)
            continue
        
        header = data.headers
        filteredData = filterData(data.text)

        if i < iterationNumber - 1:
            filteredData += ", "
        
        f.write(filteredData)
        link = cleanLink(header['link'])
    
    f.write("]")

    f.close()
    logging.info("Repos written to data.json.")

def getDailyTrending():
    f = open(DATAPATH + "daily-data.json", "w")
    link = "https://github-trending-api.now.sh/repositories?language=&since=daily"

    data = requests.get(link, headers={'Authorization': 'token ' +  GITHUB_TOKEN})

    if data.status_code != 200:
        logging.warning("Status code != 200 repo " + link)
        return False
    else:
        filteredData = filterDailyData(data.text)
        f.write((filteredData))
        f.close()
        logging.info("Daily repos written to daily-data.json.")
        return True

def addStopWords(list, words):
    for word in words:
        list.add(word)


def getReadme(inputPath, outputPath):
    with open(inputPath) as json_file:
        data = json.load(json_file)

    for repo in data:
        readmeUrl = repo['url'] + "/readme"
        if not checkUrlOrApi(readmeUrl):
            readmeUrl = "https://api.github.com/repos/" + readmeUrl[19:]

        response = requests.get(readmeUrl, headers={'Authorization': 'token ' +  GITHUB_TOKEN})

        if response.status_code != 200:
            data.remove(repo)
            logging.warning("Response code isn't 200 {}".format(readmeUrl))
            continue
        
        readmedecoded = str(base64.b64decode(json.loads(response.text)['content']))
        
        repo['readme'] = cleanReadme(readmedecoded)
 
    dumpedData = json.dumps(data)

    with open(outputPath, 'w') as json_file:
        json_file.write(dumpedData)
        logging.info("Repos Readme written to fulldata.json.")
        

def main():
    if len(GITHUB_TOKEN) < 1:
        logging.error("Please set GITHUB_TOKEN")
    elif len(sys.argv) > 1:
        nltk.download('stopwords')
        nltk.download('punkt')
        if sys.argv[1] == "default":
            if len(sys.argv) > 2:
                iterationNumber = int(sys.argv[2])
                getRepos(iterationNumber)
                getReadme(DATAPATH + "data.json", DATAPATH + "fulldata.json")
            else:
                print("Use: python {} default number_of_page".format(sys.argv[0]))
        elif sys.argv[1] == "daily":
            if getDailyTrending():
                getReadme(DATAPATH + "daily-data.json", DATAPATH + "daily-fulldata.json")
    else:
        print("Use: python {} mod number_of_page".format(sys.argv[0]))
        print("default: load batch repos")
        print("daily: load most recently repos")



if __name__ == "__main__":
    main()


