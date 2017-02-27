#!/bin/bash

export USB_PATH=$1

OLD_IFS=$IFS
IFS=$'\n'
									      #<size>#<Timestamp>#<filename>			
	listoffiles=$(sudo  ls -ltr $USB_PATH | awk 'BEGIN { OFS="#" }; {print $5,$6" "$7,$8}' | sed 1d | grep ".tar.gz")
	for file in $listoffiles
	do
		echo "$file"
	done

IFS=$OLD_IFS

