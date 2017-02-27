#!/usr/bin/python

import config
import dbhash
import httplib
import logging

_db = None

def bail(message):
    print message
    if (_db is not None):
        _db.sync()
    exit(1)

def initializeLogging():
    try:
        logging.basicConfig(filename=config.logFile, level=config.logLevel)
    except Exception as e:
        bail("Cannot get log file config parameters: " + str(e))

def getDatabase():
    global _db
    if(_db is None):
        try:
            _db = dbhash.open(config.dbFile, 'c')
        except Exception as e:
            bail("Cannot open DB: " + str(e))

        if ('failCount' not in _db):
            _db['failCount'] = '0'
            _db.sync()
    return _db

def checkHealth():
    conn = None
    if (config.healthProto is "http"):
        conn = httplib.HTTPConnection(config.healthHost)
    elif(config.healthProto is "https"):
        conn = httplib.HTTPSConnection(config.healthHost)
    else:
        bail("Health check protocol is not http or https. Proto: " + config.healthProto)

    conn.request('GET', config.healthPath)
    resp = conn.getresponse()
    if (resp.status != 200):
        logging.error("GOT FAIULURE: " + str(resp.status) + "," + resp.reason + "," + resp.read())
        return False

    return True
