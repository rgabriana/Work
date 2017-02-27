#!/bin/bash

        arg=0
        if [ -z "$1" ]
        then
                arg=1
        fi
        if [ -z "$2" ]
        then
                arg=1
        fi
        if [ $arg -gt '0' ]
        then
                echo "***********PROBLEM CALLING SCRIPT******************"
                echo "callusingCurl admin GET https://192.168.137.193/ems/api/org/user/list/admin "
                echo "************************ OR *******************************************************"
                echo "callusingCurl admin -H \"Content-Type:multipart/form-data\" -X POST https://localhost/ems/api/org/email/v1/send -F \"recipient=dhanesh.rightedinc.com\" -F \"subject=HI\" -F \"message=MSG_HERE\" -F \"from=dhanesh.rote@enlightedinc.com\" -F \"file=@/home/enlighted/em_public.key\" "
               echo "callusingCurl admin  GET https://192.168.137.193/ems/api/org/user/list/admin"
                echo "callusingCurl admin -H \"Content-Type:application/xml\" -X POST https://192.168.137.222/ems/api/org/networksettings/apply/settings -d @/home/enlighted/post.file"
        echo "for ((i=1; i <= 5 ; i++))do callusingCurl admin -H \"Content-Type:application/xml\" -X POST https://192.168.137.222/ems/api/org/networksettings/apply/settings -d @/home/enlighted/post.file;done;"
                echo "Exiting now....................................................."
                exit 1
        fi
        ### Call in the fashion callCurl admin "GET https://192.168.137.193/ems/api/org/user/list/admin"
        ts="$((` date +%s%N | cut -b1-13 ` ))"
        secretKey=`echo $(psql -q -U postgres ems -h localhost -t -c "select secret_key from users where email='$1'" sed 's,^ *,,; s, *$,,')`
        authToken=`echo -n $1$ts$secretKey | sha1sum | awk '{print $1}'`
        if [ -z $secretKey ]
        then
                echo "SEcret Key does not exists for user $1"
                return
        fi
        cmd=`echo "curl --insecure -H \"AuthenticationToken:$authToken\" -H \"ts:$ts\" -H \"UserId:$1\" ${@:2}"`
        #echo "$cmd"
        #/bin/bash -c "$cmd"
    ##curl --insecure -I --write-out "StatusCode: %{http_code}\n" --silent --output /tmp/callusingCurl -H "AuthenticationToken:$authToken" -H "ts:$ts" -H "UserId:$1" ${@:2} | grep -Fi -e StatusCode -Fi -e ETag | cut -d \" -f2
        
	curl --insecure --write-out "%{http_code}\n" --silent -I -H "AuthenticationToken:$authToken" -H "ts:$ts" -H "UserId:$1" ${@:2} 