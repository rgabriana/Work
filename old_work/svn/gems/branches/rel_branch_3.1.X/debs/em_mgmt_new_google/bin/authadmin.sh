#!/bin/bash

# Input parameters
export ac=$1
export username=$2
export cp=$3
export np=$4
export newsalt=$5
export passfile="/var/lib/tomcat6/Enlighted/adminpasswd"
export adminuserpassfile="/var/lib/tomcat6/Enlighted/adminusercredentials"

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

newadminuser() {
	newhashwithsaltanduser=${username}"="${cp}";"$newsalt
	if [ -f $adminuserpassfile ]
	then
	   echo $newhashwithsaltanduser >> $adminuserpassfile
	   echo "S"
       exit
	else
	   echo $newhashwithsaltanduser > $adminuserpassfile
	   echo "S"
       exit
	fi
	echo "F"    
	exit
}

authenticateadminuser() {
    while read p; do
		 currhashwithsaltanduser=$p
		 indexOfEqual=$(echo `expr index $currhashwithsaltanduser "="`)
		 indexOfColon=$(echo `expr index $currhashwithsaltanduser ";"`)
		 if [[ "$currhashwithsaltanduser" =~ "=" ]]
		 then
			adminuser=$(echo $currhashwithsaltanduser | cut -c1-$((indexOfEqual-1)))
			if [ "${username}" == "${adminuser}" ]
			then
			    currpass=$(echo $currhashwithsaltanduser | cut -c$((indexOfEqual+1))-$((indexOfColon-1)))
			    if [ "${cp}" == "${currpass}" ]
			    then
				echo "S"
				exit
			    else
				echo "F"
				exit
			    fi
		    fi
		 else
	        echo "F"
		    exit
         fi
    done <$adminuserpassfile
}

changeadminuserpass() {
	while read p; do
		 currhashwithsaltanduser=$p
		 indexOfEqual=$(echo `expr index $currhashwithsaltanduser "="`)
		 indexOfColon=$(echo `expr index $currhashwithsaltanduser ";"`)
		 if [[ "$currhashwithsaltanduser" =~ "=" ]]
		 then
			adminuser=$(echo $currhashwithsaltanduser | cut -c1-$((indexOfEqual-1)))
			if [ "${username}" == "${adminuser}" ]
			then
			    currpass=$(echo $currhashwithsaltanduser | cut -c$((indexOfEqual+1))-$((indexOfColon-1)))
			    if [ "${cp}" == "${currpass}" ]
			    then
				newhashwithsaltanduser=${username}"="${np}";"$newsalt
				sed -i "s/${currhashwithsaltanduser}/${newhashwithsaltanduser}/g" $adminuserpassfile
				echo "S"
				exit
			    else
				echo "F"
				exit
			    fi
		        fi
		 else
	            echo "F"
		    exit
                 fi
	done <$adminuserpassfile
	
}

deleteadminuser() {
    sed -i /${username}/d $adminuserpassfile
	echo "S"
	exit
}

if [ "${username}" == "admin" ]
then
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
else
	 if [ "${ac}" == "auth" ]
     then
         if [ -z $cp ]
         then
             echo "F"
             exit
         fi
         authenticateadminuser
     else
         if [ "${ac}" == "change" ]
         then
             if [ -z $cp -o -z $np ]
             then
                 echo "F"
                 exit
             fi
             changeadminuserpass
         else
             if [ "${ac}" == "new" ]
             then
             	newadminuser
             else
			    if [ "${ac}" == "delete" ]
             	then
             	deleteadminuser
				fi
			 fi
         fi
     fi
fi

echo "F"

}