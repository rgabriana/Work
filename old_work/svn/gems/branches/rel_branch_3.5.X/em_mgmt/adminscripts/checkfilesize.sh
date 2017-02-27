#!/bin/bash
cd /tmp/
find ./ -mmin -0.25 -user www-data | grep tmp | grep upload | head -n 1 | cut -d"/" -f2 | xargs ls -s | grep tmp | grep upload | cut -d" " -f1
