#!/bin/bash

#export USB_PATH=$1

OLD_IFS=$IFS
IFS=$'\n'
									      #<size>#<Timestamp>#<filename>			
	listoffiles=$(df -k | grep media | tr -s ' ' ' '| cut -f6 -d' ')
	for file in $listoffiles
	do
		echo $file
	done

IFS=$OLD_IFS