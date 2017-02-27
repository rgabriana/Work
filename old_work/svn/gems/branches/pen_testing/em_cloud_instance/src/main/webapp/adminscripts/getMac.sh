#!/bin/bash

ifconfig | grep HWaddr | grep eth0 | head -n 1 | sed 's/^.*HWaddr\ *//'