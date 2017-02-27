#!/bin/bash

# Input parameters
export ac=$1
export cp=$2
export np=$3
export newsalt=$4
export passfile="/var/lib/tomcat6/Enlighted/adminpasswd"

{

reset() {
    #changed the hashing to sha1
	hashedpassword=$(echo -n \a\d\m\i\n | md5sum | cut -d" " -f1)
    echo $hashedpassword > $passfile
    echo "S"
    exit
}

authenticate() {
	currhashwithsalt=$(head -n 1 $passfile)
	index1=$(echo `expr index $currhashwithsalt ";"`)
	if [[ "$currhashwithsalt" =~ ";" ]]
	then
	    currpass=$(echo $currhashwithsalt | cut -c1-$((index1-1)))        
	else
	    currpass=$currhashwithsalt    
	fi
	
	
    if [ "${cp}" == "${currpass}" ]
    then
        echo "S"
        exit
    else
        echo "F"
        exit
    fi
}

changepass() {
	currhashwithsalt=$(head -n 1 $passfile)
	index1=$(echo `expr index $currhashwithsalt ";"`)
	if [[ "$currhashwithsalt" =~ ";" ]]
	then
		currpass=$(echo $currhashwithsalt | cut -c1-$((index1-1)))
	    
	else
		currpass=$currhashwithsalt
	    	    
	fi
	
	if [ "${cp}" == "${currpass}" ]
    then
        echo -n ${np}";"$newsalt > $passfile
        echo "S"
        exit
    else
        echo "F"
        exit
    fi
}



if [ "${ac}" == "auth" ]
then
    if [ -z $cp ]
    then
        echo "F"
        exit
    fi
    authenticate
else
    if [ "${ac}" == "change" ]
    then
        if [ -z $cp -o -z $np ]
        then
            echo "F"
            exit
        fi
        changepass
    else
        if [ "${ac}" == "reset" ]
        then
            reset
        fi
    fi
fi

echo "F"

}
