#!/usr/bin/python

import AirlinkGX440
import config
import logging
import utils

def main():
    utils.initializeLogging()

    gateway = AirlinkGX440.AirlinkGX440(config.gwHost, config.gwUser, config.gwPass)
    logging.debug('Rebooting gateway')
    gateway.reboot()
    exit(0)

main()
