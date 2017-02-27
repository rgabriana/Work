#!/usr/bin/python

import AirlinkGX440
import config
import logging
import utils

def main():
    utils.initializeLogging()
    data = utils.getDatabase()

    gateway = AirlinkGX440.AirlinkGX440(config.gwHost, config.gwUser, config.gwPass)

    if (not utils.checkHealth()):
        data['failCount'] = str(int(data['failCount']) + 1)
        logging.debug("Fail count: " + data['failCount'] + ", Threshold: " + str(config.failureThresh))
    
        if(int(data['failCount']) >= config.failureThresh):
            gateway.reboot()
            data['failCount'] = '0'
    else:
        logging.debug('Health check success')
        data['failCount'] = '0'
    
    data.sync()
    exit(0)

main()
