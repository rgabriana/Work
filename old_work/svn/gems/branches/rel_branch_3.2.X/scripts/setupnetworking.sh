#!/bin/bash
sudo /usr/bin/killall -KILL dhclient
sudo /usr/bin/killall -KILL dhclient3
sudo cp ${1} /etc/network/interfaces
sudo /etc/init.d/networking restart



