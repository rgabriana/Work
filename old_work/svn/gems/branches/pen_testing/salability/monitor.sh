#!/bin/bash

while :
do
top -b -n 1 >> results.txt
echo -e "\n" >> results.txt
free >> results.txt
echo -e "\n" >> results.txt
sleep 10
done
