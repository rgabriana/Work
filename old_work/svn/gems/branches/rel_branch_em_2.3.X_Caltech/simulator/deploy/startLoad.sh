#!/bin/sh

java -Djava.util.logging.config.file=logging.properties -jar Simulator.jar -c eth0:1 169.254.0.1 10000 b2:c3:3b 240000
