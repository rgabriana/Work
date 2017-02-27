#!/bin/bash
cd $1
find ./ -mmin -0.25 | grep tmp | head -n 1 | cut -d"/" -f2 | xargs ls -s | grep tmp | cut -d" " -f1
