#!/usr/bin/python
    
from http.client import HTTPConnection
import json

class Path:
    DIR = 'res/raw'
    def __init__(self, url):
        self.url = url
    def getWebUrl(self):
        return "http://mobilewin.csse.rose-hulman.edu:5600/{0}".format(self.url)
    def getFilePath(self):
        if '?' in self.url:
            path, args = self.url.split('?')
            path = path.replace('/', '_')
            url = '{0}__{1}'.format(path, args.replace('=', '_'))
        else:
            path, args = self.url, ''
            path = path.replace('/', '_')
            url = path
        return "{0}/{1}".format(Path.DIR, url)
    def __str__(self):
        return '/' + self.url
    def __repr__(self):
        return "Path('{0}')".format(self.url)

def writeFile(path, data):
    fout = open(path.getFilePath(), 'w')
    fout.write(data)
    fout.close()

def readFromServer(path):
    conn = HTTPConnection('mobilewin.csse.rose-hulman.edu', port=5600)
    conn.request('GET', str(path))
    response = conn.getresponse()
    data = response.read()
    return str(data, 'UTF-8')

def toJson(data):
    return json.loads(data)

def ids(data):
    for item in data:
        yield item['Id']

#get the top locations
topPath = Path('locations/data/top')
topData = readFromServer(topPath)
topJson = toJson(topData)
writeFile(topPath, topData)

#get the inner locations
for id in ids(topJson['Locations']):
    path = Path('locations/data/within/{0}'.format(id))
    data = readFromServer(path)
    writeFile(path, data)
    print("Wrote: {0}".format(id))

#run some searches
def runSearch(query):
    query = query.lower()
    path = Path('locations/names?s={0}'.format(query))
    data = readFromServer(path)
    writeFile(path, data)
    print("Searched: {0}".format(query))

runSearch('Hall')


