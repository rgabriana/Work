#!/bin/bash

file=$1
path=$2
TOMCAT_PATH=$3
FLOW=$4

SUCCESS=1
FAILURE=2


cd $path
filepresent=$(tar -tf $file EMSMANIFEST.MF > /dev/null 2>&1;echo $?)

if [ $filepresent -ne 0 ]
then
	echo "-1"
	exit 2
else
	tar -zxf $file EMSMANIFEST.MF
	versionintar=$(cat EMSMANIFEST.MF | grep "Build-Version" | sed -re 's/^.+: //' )
	versiononsystem=$(cat $TOMCAT_PATH/ems/META-INF/EMSMANIFEST.MF | grep "Build-Version" | sed -re 's/^.+: //')
	
	rm -f EMSMANIFEST.MF

	echo "tarversion##$versionintar"
	echo "systemversion##$versiononsystem"
	exit $SUCCESS
fi	
