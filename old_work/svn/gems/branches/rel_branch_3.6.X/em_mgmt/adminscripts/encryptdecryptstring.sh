#!/bin/bash

# Input parameters
export OPERATION="$1"
export STRING=$2
PASSWORD="enLighted-SaveEnergy"

encryptStringfunction() {
	stringtoencrypt=$1
	password=$2
	encyptedstring=""
	encyptedstring=$(echo ${stringtoencrypt} | openssl enc -aes-256-cbc -a -salt -pass pass:$password)
	echo "$encyptedstring"
}

decryptStringfunction() {
	stringtodecrypt=$1
	password=$2
	decyptedstring=""
	decyptedstring=$(echo ${stringtodecrypt} | openssl enc -aes-256-cbc -d -a -salt -pass pass:$password)
	echo "$decyptedstring"
}

if [ "$OPERATION" == "encrypt" ]
then
    encryptStringfunction $STRING $PASSWORD
fi

if [ "$OPERATION" == "decrypt" ]
then
    decryptStringfunction $STRING $PASSWORD
fi

exit 0
