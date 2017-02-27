#!/bin/bash
export KEY_DIR="/var/lib/tomcat6/Enlighted/"
{
# Input parameters
export ACTION="$1"
export SECURITYKEY="$2"

saveKey() {
   rm -rf $KEY_DIR/recoverykey.key
   touch $KEY_DIR/recoverykey.key
   OUT=$?
   if [ $OUT -eq 0 ]
   then
   		echo -n $SECURITYKEY >> $KEY_DIR/recoverykey.key
   		OUT=$?
   		echo $OUT
   fi
}

if [ $ACTION=="save" ]
then
    saveKey
fi

}
