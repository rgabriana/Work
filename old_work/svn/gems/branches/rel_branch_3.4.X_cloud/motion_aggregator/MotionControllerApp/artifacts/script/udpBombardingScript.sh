#!/bin/bash
while true
do
sudo sendip -p ipv4 -is 192.168.1.80 -p udp -us 8085 -ud 8084 -d  0x434D53001C6854f500d945000000010000009A0000004A931B454E44 -v 192.168.1.80
sleep 0.5
done
