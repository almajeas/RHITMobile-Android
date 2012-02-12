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
        return "{0}/response_{1}".format(Path.DIR, url)
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

def cachePath(path):
    data = readFromServer(path)
    res = toJson(data)
    writeFile(path, data)
    print("{0}".format(path))
    return res

#get root
cachePath(Path(''))

#get tour tags
cachePath(Path('tours/tags'))

#get campus services
cachePath(Path('services'))

#get the top locations
topJson = cachePath(Path('locations/data/top'))

#get the inner locations
for id in ids(topJson['Locations']):
    cachePath(Path('locations/data/within/{0}'.format(id)))

#run some searches
def runSearch(query):
    query = query.lower()
    cachePath(Path('locations/names?s={0}'.format(query)))
runSearch('Hall')
runSearch('Olin')
runSearch('Union')


