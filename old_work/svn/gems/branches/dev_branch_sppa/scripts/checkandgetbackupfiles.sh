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
#-- To be used later
 			#tar -zxf $file EMSMANIFEST.MF > /dev/null 2>&1 | sed 1d
                        #tarversion=$(cat EMSMANIFEST.MF | grep "Build-Version" | sed -re 's/^.+: //')
                        #versiononsystem=$(cat $TOMCAT_PATH/ems/META-INF/EMSMANIFEST.MF | grep Build-Version | sed -re 's/^.+: //')
                        #rm -f EMSMANIFEST.MF

                        #-- Commenting out version check for now.

                        #if [ $tarversion != $versiononsystem ]
                        #then
                                #continue
                        #else
